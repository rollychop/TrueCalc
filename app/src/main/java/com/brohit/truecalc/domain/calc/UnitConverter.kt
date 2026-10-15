package com.brohit.truecalc.domain.calc

/**
 * A unit as a linear map to its category's base unit: `base = value × factor + offset`.
 * Only temperature uses [offset]. [id] is stable (stored in history), unique within a category.
 */
data class MeasureUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val factor: Double,
    val offset: Double = 0.0,
) {
    fun toBase(value: Double): Double = value * factor + offset
    fun fromBase(base: Double): Double = (base - offset) / factor
}

/**
 * Unit categories with their conversion tables. [key] is stable (stored in history).
 * Sources: exact SI/international definitions (1 in = 2.54 cm, 1 lb = 0.45359237 kg,
 * US customary gallon = 231 in³, imperial gallon = 4.54609 L).
 */
enum class UnitCategory(val key: String, val label: String, val units: List<MeasureUnit>) {
    Length(
        "length", "Length",
        listOf(
            MeasureUnit("mm", "Millimetre", "mm", 0.001),
            MeasureUnit("cm", "Centimetre", "cm", 0.01),
            MeasureUnit("m", "Metre", "m", 1.0),
            MeasureUnit("km", "Kilometre", "km", 1_000.0),
            MeasureUnit("in", "Inch", "in", 0.0254),
            MeasureUnit("ft", "Foot", "ft", 0.3048),
            MeasureUnit("yd", "Yard", "yd", 0.9144),
            MeasureUnit("mi", "Mile", "mi", 1_609.344),
            MeasureUnit("nmi", "Nautical mile", "nmi", 1_852.0),
        ),
    ),
    Weight(
        "weight", "Weight",
        listOf(
            MeasureUnit("mg", "Milligram", "mg", 1e-6),
            MeasureUnit("g", "Gram", "g", 0.001),
            MeasureUnit("kg", "Kilogram", "kg", 1.0),
            MeasureUnit("quintal", "Quintal", "q", 100.0),
            MeasureUnit("t", "Tonne", "t", 1_000.0),
            MeasureUnit("tola", "Tola", "tola", 0.0116638125),
            MeasureUnit("oz", "Ounce", "oz", 0.028349523125),
            MeasureUnit("lb", "Pound", "lb", 0.45359237),
            MeasureUnit("st", "Stone", "st", 6.35029318),
        ),
    ),
    Temperature(
        "temperature", "Temperature",
        listOf(
            MeasureUnit("c", "Celsius", "°C", 1.0),
            MeasureUnit("f", "Fahrenheit", "°F", 5.0 / 9.0, -160.0 / 9.0),
            MeasureUnit("k", "Kelvin", "K", 1.0, -273.15),
        ),
    ),
    Area(
        "area", "Area",
        listOf(
            MeasureUnit("cm2", "Square centimetre", "cm²", 1e-4),
            MeasureUnit("m2", "Square metre", "m²", 1.0),
            MeasureUnit("km2", "Square kilometre", "km²", 1e6),
            MeasureUnit("in2", "Square inch", "sq in", 0.00064516),
            MeasureUnit("ft2", "Square foot", "sq ft", 0.09290304),
            MeasureUnit("yd2", "Square yard (gaj)", "sq yd", 0.83612736),
            MeasureUnit("cent", "Cent", "cent", 40.468564224),
            MeasureUnit("guntha", "Guntha", "guntha", 101.17141056),
            MeasureUnit("bigha", "Bigha (27,000 sq ft)", "bigha", UnitConverter.BIGHA_SQ_FT * 0.09290304),
            MeasureUnit("acre", "Acre", "ac", 4_046.8564224),
            MeasureUnit("ha", "Hectare", "ha", 10_000.0),
            MeasureUnit("mi2", "Square mile", "sq mi", 2_589_988.110336),
        ),
    ),
    Volume(
        "volume", "Volume",
        listOf(
            MeasureUnit("ml", "Millilitre", "mL", 0.001),
            MeasureUnit("l", "Litre", "L", 1.0),
            MeasureUnit("m3", "Cubic metre", "m³", 1_000.0),
            MeasureUnit("tsp", "Teaspoon (US)", "tsp", 0.00492892159375),
            MeasureUnit("tbsp", "Tablespoon (US)", "tbsp", 0.01478676478125),
            MeasureUnit("floz", "Fluid ounce (US)", "fl oz", 0.0295735295625),
            MeasureUnit("cup", "Cup (US)", "cup", 0.2365882365),
            MeasureUnit("pt", "Pint (US)", "pt", 0.473176473),
            MeasureUnit("gal", "Gallon (US)", "gal", 3.785411784),
            MeasureUnit("galuk", "Gallon (UK)", "gal (UK)", 4.54609),
            MeasureUnit("in3", "Cubic inch", "in³", 0.016387064),
            MeasureUnit("ft3", "Cubic foot", "ft³", 28.316846592),
        ),
    ),
    Speed(
        "speed", "Speed",
        listOf(
            MeasureUnit("mps", "Metre per second", "m/s", 1.0),
            MeasureUnit("kmph", "Kilometre per hour", "km/h", 1.0 / 3.6),
            MeasureUnit("mph", "Mile per hour", "mph", 0.44704),
            MeasureUnit("fps", "Foot per second", "ft/s", 0.3048),
            MeasureUnit("knot", "Knot", "kn", 1_852.0 / 3_600.0),
        ),
    ),
    Data(
        "data", "Data",
        listOf(
            MeasureUnit("bit", "Bit", "bit", 0.125),
            MeasureUnit("B", "Byte", "B", 1.0),
            MeasureUnit("KB", "Kilobyte (1000 B)", "KB", 1e3),
            MeasureUnit("MB", "Megabyte (1000 KB)", "MB", 1e6),
            MeasureUnit("GB", "Gigabyte (1000 MB)", "GB", 1e9),
            MeasureUnit("TB", "Terabyte (1000 GB)", "TB", 1e12),
            MeasureUnit("KiB", "Kibibyte (1024 B)", "KiB", 1_024.0),
            MeasureUnit("MiB", "Mebibyte (1024 KiB)", "MiB", 1_048_576.0),
            MeasureUnit("GiB", "Gibibyte (1024 MiB)", "GiB", 1_073_741_824.0),
            MeasureUnit("TiB", "Tebibyte (1024 GiB)", "TiB", 1_099_511_627_776.0),
        ),
    ),
    Time(
        "time", "Time",
        listOf(
            MeasureUnit("ms", "Millisecond", "ms", 0.001),
            MeasureUnit("s", "Second", "s", 1.0),
            MeasureUnit("min", "Minute", "min", 60.0),
            MeasureUnit("h", "Hour", "h", 3_600.0),
            MeasureUnit("day", "Day", "d", 86_400.0),
            MeasureUnit("week", "Week", "wk", 604_800.0),
            MeasureUnit("month", "Month (avg)", "mo", UnitConverter.SECONDS_PER_YEAR / 12.0),
            MeasureUnit("year", "Year (avg)", "yr", UnitConverter.SECONDS_PER_YEAR),
        ),
    );

    fun unit(id: String?): MeasureUnit? = units.firstOrNull { it.id == id }

    /** Sensible starting pair for the picker (e.g. km → mi, °C → °F). */
    val defaultFrom: MeasureUnit get() = unit(DEFAULTS.getValue(this).first)!!
    val defaultTo: MeasureUnit get() = unit(DEFAULTS.getValue(this).second)!!

    companion object {
        fun fromKey(key: String?): UnitCategory = entries.firstOrNull { it.key == key } ?: Length

        private val DEFAULTS: Map<UnitCategory, Pair<String, String>> = mapOf(
            Length to ("km" to "mi"),
            Weight to ("kg" to "lb"),
            Temperature to ("c" to "f"),
            Area to ("ft2" to "m2"),
            Volume to ("l" to "gal"),
            Speed to ("kmph" to "mph"),
            Data to ("GB" to "MB"),
            Time to ("h" to "min"),
        )
    }
}

/** Converts values between units of one [UnitCategory]. */
object UnitConverter {

    /** Gregorian average year: 365.2425 days. */
    const val SECONDS_PER_YEAR: Double = 365.2425 * 86_400.0

    /** Common value used for bigha: 27,000 sq ft (it varies a lot by state). */
    const val BIGHA_SQ_FT: Double = 27_000.0

    fun convert(value: Double, from: MeasureUnit, to: MeasureUnit): Double {
        if (from == to) return value
        return to.fromBase(from.toBase(value))
    }

    /** [value] in [from] expressed in every unit of [category], in table order. */
    fun convertToAll(value: Double, from: MeasureUnit, category: UnitCategory): List<Pair<MeasureUnit, Double>> =
        category.units.map { it to convert(value, from, it) }

    /** Display text: up to 8 significant digits, Indian grouping, E notation for extremes. */
    fun format(value: Double): String =
        CalcFormat.significant(value, digits = 8, grouping = true, maxPlainExponent = 12, minPlainExponent = -6)
}
