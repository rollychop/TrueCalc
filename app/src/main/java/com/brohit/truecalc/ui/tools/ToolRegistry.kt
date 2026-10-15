package com.brohit.truecalc.ui.tools

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.brohit.truecalc.common.TextSearch
import com.brohit.truecalc.ui.navigation.Route
import com.brohit.truecalc.ui.screens.compund_interest.CalculationType
import com.brohit.truecalc.ui.theme.LocalExtendedColors
import kotlinx.serialization.json.Json

/** Which theme colour group tints a category's icons. */
enum class CategoryAccent { Primary, Secondary, Tertiary, Extended }

enum class ToolCategory(val title: String, val icon: ImageVector, val accent: CategoryAccent) {
    Finance("Finance", Icons.Filled.AccountBalanceWallet, CategoryAccent.Primary),
    Health("Health", Icons.Filled.MonitorHeart, CategoryAccent.Tertiary),
    Everyday("Everyday", Icons.Filled.Calculate, CategoryAccent.Secondary),
    Convert("Convert", Icons.Filled.SwapHoriz, CategoryAccent.Extended),
    Math("Math", Icons.Filled.Functions, CategoryAccent.Extended),
}

/**
 * Container and content colours for a category's icon tile, from the active theme
 * (brand, dynamic, light or dark). Returns `container to content`.
 */
@Composable
@ReadOnlyComposable
fun categoryColors(category: ToolCategory): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (category.accent) {
        CategoryAccent.Primary -> scheme.primaryContainer to scheme.onPrimaryContainer
        CategoryAccent.Secondary -> scheme.secondaryContainer to scheme.onSecondaryContainer
        CategoryAccent.Tertiary -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        CategoryAccent.Extended -> LocalExtendedColors.current.let {
            it.accentContainer to it.onAccentContainer
        }
    }
}

/**
 * Strong accent and its "on" colour for a category (e.g. a filled icon circle), from the active
 * theme. Returns `accent to onAccent`.
 */
@Composable
@ReadOnlyComposable
fun categoryAccentColors(category: ToolCategory): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (category.accent) {
        CategoryAccent.Primary -> scheme.primary to scheme.onPrimary
        CategoryAccent.Secondary -> scheme.secondary to scheme.onSecondary
        CategoryAccent.Tertiary -> scheme.tertiary to scheme.onTertiary
        CategoryAccent.Extended -> LocalExtendedColors.current.let { it.accent to it.onAccent }
    }
}

/**
 * One tool in the app.
 *
 * @param id stable identifier; never change it once shipped (future favourites / assistant
 *   `tools.json` key on it).
 * @param route navigation target without restoration state.
 */
data class ToolSpec(
    val id: String,
    val name: String,
    val description: String,
    val category: ToolCategory,
    val icon: ImageVector,
    val route: Route,
    val keywords: List<String> = emptyList(),
    val isNew: Boolean = false,
) {
    /**
     * The route serialised as `Json.encodeToString<Route>(route)`: the key favourites, tool usage
     * and history use in Room (e.g. `{"type":"EmiCalculator"}`). Never change this format, or
     * existing pins and history stop matching.
     */
    val routeKey: String get() = Json.encodeToString(Route.serializer(), route)
}

/**
 * Whether this route points to the same tool as [other], ignoring restoration state.
 * CompoundInterest backs several tools, so its calculationType is part of the identity.
 */
fun Route.isSameTool(other: Route): Boolean {
    if (this::class != other::class) return false
    if (this is Route.Calculator.CompoundInterest && other is Route.Calculator.CompoundInterest) {
        return calculationType == other.calculationType
    }
    return true
}

/** Single source of truth for the tool list (Home, Search, Favourites, History labels…). */
object ToolRegistry {

    val all: List<ToolSpec> = listOf(
        ToolSpec(
            id = "emi",
            name = "EMI Calculator",
            description = "Monthly installment, total interest and payment for a loan",
            category = ToolCategory.Finance,
            icon = Icons.Filled.AttachMoney,
            route = Route.Calculator.Emi(),
            keywords = listOf(
                "loan", "home loan", "car loan", "personal loan", "mortgage", "installment",
                "instalment", "equated monthly installment", "processing fee", "repayment",
            ),
        ),
        ToolSpec(
            id = "compound_interest",
            name = "Compound Interest",
            description = "Growth of a lump sum with interest compounded over time",
            category = ToolCategory.Finance,
            icon = Icons.Filled.MonetizationOn,
            route = Route.Calculator.CompoundInterest(calculationType = CalculationType.COMPOUND),
            keywords = listOf("ci", "investment", "growth", "lumpsum", "lump sum", "returns", "savings"),
        ),
        ToolSpec(
            id = "simple_interest",
            name = "Simple Interest",
            description = "Flat interest on a principal for a period",
            category = ToolCategory.Finance,
            icon = Icons.Filled.Money,
            route = Route.Calculator.CompoundInterest(calculationType = CalculationType.SIMPLE),
            keywords = listOf("si", "flat interest", "principal rate time", "borrow", "lend"),
        ),
        ToolSpec(
            id = "fixed_deposit",
            name = "Fixed Deposit",
            description = "FD maturity amount and interest earned",
            category = ToolCategory.Finance,
            icon = Icons.Filled.Savings,
            route = Route.Calculator.CompoundInterest(calculationType = CalculationType.FIXED),
            keywords = listOf("fd", "bank deposit", "term deposit", "maturity", "savings", "bank"),
        ),
        ToolSpec(
            id = "recurring_deposit",
            name = "Recurring Deposit",
            description = "Maturity of regular monthly (or other) deposits",
            category = ToolCategory.Finance,
            icon = Icons.Filled.Timeline,
            route = Route.Calculator.RecurringDeposit(),
            keywords = listOf("rd", "monthly deposit", "recurring", "maturity", "savings", "post office"),
        ),
        ToolSpec(
            id = "bmi",
            name = "BMI Calculator",
            description = "Body Mass Index from weight and height",
            category = ToolCategory.Health,
            icon = Icons.Filled.Accessibility,
            route = Route.Calculator.Bmi(),
            keywords = listOf("body mass index", "weight", "height", "obesity", "overweight", "fitness"),
        ),
        ToolSpec(
            id = "calories",
            name = "Calorie Calculator",
            description = "Daily calorie needs (BMR and activity)",
            category = ToolCategory.Health,
            icon = Icons.Filled.Restaurant,
            route = Route.Calculator.Calorie(),
            keywords = listOf("calories", "kcal", "bmr", "tdee", "diet", "metabolism", "energy"),
        ),
        ToolSpec(
            id = "ideal_weight",
            name = "Ideal Weight Calculator",
            description = "Healthy body weight for your height",
            category = ToolCategory.Health,
            icon = Icons.Filled.Scale,
            route = Route.Calculator.IdealWeight(),
            keywords = listOf("ideal body weight", "ibw", "healthy weight", "target weight", "devine"),
        ),
    )

    private val byIdMap: Map<String, ToolSpec> = all.associateBy { it.id }

    init {
        require(byIdMap.size == all.size) { "Duplicate ToolSpec id in ToolRegistry" }
    }

    fun byId(id: String): ToolSpec? = byIdMap[id]

    /** Tool for a route, ignoring restoration state; CompoundInterest variants are distinct. */
    fun forRoute(route: Route): ToolSpec? = all.firstOrNull { it.route.isSameTool(route) }

    /**
     * Tool for a stored route key (route JSON from favourites, usage or history), or null if it
     * can't be decoded or no longer maps to a registered tool.
     */
    fun forRouteKey(routeKey: String): ToolSpec? {
        val route = try {
            Json.decodeFromString(Route.serializer(), routeKey)
        } catch (_: Exception) {
            return null
        }
        return forRoute(route)
    }

    /**
     * Resolves stored route keys to tools, in order, dropping unknown keys and duplicates of the
     * same tool, and keeping at most [limit].
     */
    fun forRouteKeys(routeKeys: List<String>, limit: Int = Int.MAX_VALUE): List<ToolSpec> =
        routeKeys.asSequence()
            .mapNotNull { forRouteKey(it) }
            .distinctBy { it.id }
            .take(limit)
            .toList()

    /** Category for a stable key ([ToolCategory.name], e.g. "Finance"), or null. */
    fun categoryForKey(key: String): ToolCategory? = ToolCategory.entries.firstOrNull { it.name == key }

    /** Tools in [category], in registry order. */
    fun inCategory(category: ToolCategory): List<ToolSpec> = all.filter { it.category == category }

    /** Tools grouped by category, in [ToolCategory] order; empty categories are omitted. */
    fun byCategory(): Map<ToolCategory, List<ToolSpec>> =
        ToolCategory.entries
            .associateWith { category -> all.filter { it.category == category } }
            .filterValues { it.isNotEmpty() }

    /** Case-insensitive search over name, keywords and description, best match first. */
    fun search(query: String): List<ToolSpec> = TextSearch.rank(
        items = all,
        query = query,
        name = { it.name },
        keywords = { it.keywords },
        description = { it.description },
    )
}
