#!/usr/bin/env python3
"""Desktop feasibility check for the TrueCalc smart assistant.

Runs every query in test_queries.jsonl through Needle with the tools in
tools.json and scores the tool calls against the expected ones.

    pip install cactus-needle
    python spike.py                       # base Needle 3
    python spike.py --weights tuned.cact  # a fine-tuned model
    python spike.py --no-normalize        # see how the raw model copes with "25 lakh"
    python spike.py -v                    # print every query, not just failures

normalize() is the reference for the Kotlin QueryNormalizer that will run on
device before the text reaches the engine; keep the two in sync.
"""
import argparse
import json
import os
import re
import sys
import time
from pathlib import Path

HERE = Path(__file__).resolve().parent

_NUM = r"(\d+(?:\.\d+)?)"
_MULTIPLIERS = [
    (rf"{_NUM}\s*(?:crores?|cr)\b", 10_000_000),
    (rf"{_NUM}\s*(?:lakhs?|lacs?|l)\b", 100_000),
    (rf"{_NUM}\s*k\b", 1_000),
]


def _fmt(value: float) -> str:
    return str(int(round(value))) if abs(value - round(value)) < 1e-9 else f"{value:.1f}"


def normalize(text: str) -> str:
    """Rewrite Indian amount shorthand and imperial heights into plain numbers.

    The engine rejects numbers that do not appear in the source text, so
    "25 lakh" has to reach it as "2500000".
    """
    t = text
    # Grouped digits: 2,00,000 / 5,00,000 / 75,000 -> 200000 / 500000 / 75000
    t = re.sub(r"\d{1,3}(?:,\d{2,3})+\b", lambda m: m.group(0).replace(",", ""), t)
    t = re.sub(r"₹\s*", "", t)
    for pattern, factor in _MULTIPLIERS:
        t = re.sub(pattern, lambda m, f=factor: _fmt(float(m.group(1)) * f), t, flags=re.I)
    # 5 ft 9 in / 5'9" / 5 feet 9 inches -> 175.3 cm
    t = re.sub(
        r"(\d)\s*(?:ft|feet|foot|')\s*(\d{1,2})?\s*(?:in|inch|inches|\"|'')?(?=\W|$)",
        lambda m: _fmt((int(m.group(1)) * 12 + int(m.group(2) or 0)) * 2.54) + " cm",
        t,
        flags=re.I,
    )
    return t


def _defaults(tools):
    return {
        tool["name"]: {k: v.get("default") for k, v in tool["parameters"]["properties"].items()}
        for tool in tools
    }


def _same(expected, actual) -> bool:
    if isinstance(expected, (int, float)) and not isinstance(expected, bool):
        try:
            a = float(actual)
        except (TypeError, ValueError):
            return False
        return abs(a - expected) <= max(0.5, abs(expected) * 0.005)
    return str(expected).strip().lower() == str(actual).strip().lower()


def score(expected_calls, actual_calls, defaults):
    """Return (ok, reason). Calls must match in order; omitted args fall back to schema defaults."""
    if [c["name"] for c in expected_calls] != [c["name"] for c in actual_calls]:
        return False, "tool"
    for exp, act in zip(expected_calls, actual_calls):
        schema_defaults = defaults.get(exp["name"], {})
        act_args = act.get("arguments") or {}
        for key, default in schema_defaults.items():
            want = exp["arguments"].get(key, default)
            got = act_args.get(key, default)
            if want is None:
                continue
            if got is None or not _same(want, got):
                return False, f"arg {key}: want {want!r} got {got!r}"
    return True, ""


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--tools", default=HERE / "tools.json")
    ap.add_argument("--queries", default=HERE / "test_queries.jsonl")
    ap.add_argument("--weights", default=None, help="tuned .cact; default is the base model")
    ap.add_argument("--generation", type=int, default=None, help="2 for Needle 2")
    ap.add_argument("--threshold", type=float, default=0.7, help="confidence to act without confirming")
    ap.add_argument("--no-normalize", action="store_true")
    ap.add_argument("-v", "--verbose", action="store_true")
    args = ap.parse_args()

    os.environ.setdefault("NEEDLE_TELEMETRY", "0")
    os.environ.setdefault("DO_NOT_TRACK", "1")
    import needle

    tools = json.loads(Path(args.tools).read_text())
    cases = [json.loads(line) for line in Path(args.queries).read_text().splitlines() if line.strip()]
    defaults = _defaults(tools)

    kwargs = dict(tools=tools, system="locale: en-IN; device: phone", stateless=True)
    if args.weights:
        kwargs["weights"] = args.weights
    if args.generation:
        kwargs["generation"] = args.generation
    agent = needle.Needle(**kwargs)

    passed = act_ok = act_wrong = confirm = refused_ok = 0
    latencies = []
    for case in cases:
        text = case["query"] if args.no_normalize else normalize(case["query"])
        start = time.perf_counter()
        r = agent.complete(text)
        latencies.append((time.perf_counter() - start) * 1000)

        calls = r.get("function_calls") or []
        held = r.get("suppressed_calls") or []
        conf = r.get("confidence")
        ok, reason = score(case["expected"], calls or held, defaults)
        passed += ok
        if not case["expected"] and not calls:
            refused_ok += 1
        elif calls and conf is not None and conf >= args.threshold:
            act_ok += ok
            act_wrong += not ok
        else:
            confirm += 1

        if args.verbose or not ok:
            mark = "PASS" if ok else "FAIL"
            print(f"[{mark}] conf={conf} {latencies[-1]:.0f}ms  {case['query']}")
            if text != case["query"]:
                print(f"        normalized: {text}")
            if not ok:
                print(f"        {reason}")
                print(f"        got:  {json.dumps(calls or held)}")
                print(f"        want: {json.dumps(case['expected'])}")

    n = len(cases)
    latencies.sort()
    print()
    print(f"Accuracy (exact calls):           {passed}/{n} = {passed / n:.0%}")
    print(f"Auto-run at conf>={args.threshold}: correct {act_ok}, WRONG {act_wrong}")
    print(f"Would ask user to confirm:        {confirm}")
    print(f"Correct refusals:                 {refused_ok}/{sum(1 for c in cases if not c['expected'])}")
    print(f"Latency p50 / p95 (desktop):      {latencies[n // 2]:.0f} / {latencies[int(n * 0.95) - 1]:.0f} ms")
    print()
    print("Go/no-go: accuracy >= 85% and 'Auto-run WRONG' == 0 (see README.md, Phase 0).")
    return 0 if passed / n >= 0.85 and act_wrong == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
