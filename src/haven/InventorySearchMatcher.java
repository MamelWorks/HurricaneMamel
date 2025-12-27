package haven;

import haven.res.ui.stackinv.ItemStack;
import haven.resutil.Curiosity;
import haven.resutil.FoodInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for matching items against inventory search queries.
 * Supports both name-based fuzzy matching and category-based property matching.
 */
public class InventorySearchMatcher {
	// Pattern for matching property queries like "lp:>100", "fep:50", or "fep:str"
	// Group 1: property (lp, lph, fep, food, study)
	// Group 2: FEP type name (str, agi, etc.) - text only
	// Group 3: operator (>, <, >=, etc.)
	// Group 4: numeric value
	private static final Pattern PROPERTY_PATTERN = Pattern.compile("^(lp|lph|fep|food|study):?(?:([a-zA-Z]+)|([><=]+)?(\\d+\\.?\\d*))?$", Pattern.CASE_INSENSITIVE);

	/**
	 * Checks if this item should be highlighted based on search query.
	 * Handles both closed stacks (container items) and items inside open stacks.
	 *
	 * @param item The GItem to check
	 * @param searchQuery The search string (e.g., "lp:>100", "fep:", "bronze")
	 * @return true if this item should be highlighted
	 */
	public static boolean matchesItemOrStack(GItem item, String searchQuery) {
		if (searchQuery == null || searchQuery.trim().isEmpty()) {
			return false;
		}

		// Case 1: This item is inside an open stack
		// If ANY item in the stack matches, highlight ALL items in the stack
		if (item.parent instanceof ItemStack) {
			ItemStack stack = (ItemStack) item.parent;
			for (GItem stackItem : stack.order) {
				if (matches(stackItem, searchQuery)) {
					return true;
				}
			}
			return false;
		}

		// Case 2: This is a closed stack (container item)
		// Check if any items inside the stack match
		if (item.contents != null && item.contents instanceof ItemStack) {
			ItemStack stack = (ItemStack) item.contents;
			for (GItem stackItem : stack.order) {
				if (matches(stackItem, searchQuery)) {
					return true; // Highlight the container because it contains matching items
				}
			}
			return false;
		}

		// Case 3: Regular item (not a stack)
		return matches(item, searchQuery);
	}

	/**
	 * Checks if an item matches the given search query.
	 *
	 * @param item The GItem to check
	 * @param searchQuery The search string (e.g., "lp:>100", "fep:", "bronze")
	 * @return true if the item matches the query
	 */
	public static boolean matches(GItem item, String searchQuery) {
		if (searchQuery == null || searchQuery.trim().isEmpty()) {
			return false;
		}

		searchQuery = searchQuery.trim().toLowerCase();

		// Check if this is a property-based query
		Matcher matcher = PROPERTY_PATTERN.matcher(searchQuery);
		if (matcher.matches()) {
			String property = matcher.group(1);
			String fepType = matcher.group(2);  // For fep:str, fep:agi, etc.
			String operator = matcher.group(3);
			String valueStr = matcher.group(4);

			return matchesProperty(item, property, fepType, operator, valueStr);
		}

		// Default to name-based fuzzy matching
		return matchesName(item, searchQuery);
	}

	/**
	 * Matches item against a property-based query (lp:, fep:, etc.)
	 */
	private static boolean matchesProperty(GItem item, String property, String fepType, String operator, String valueStr) {
		switch (property.toLowerCase()) {
			case "lp":
			case "study":
				return matchesLearningPoints(item, operator, valueStr);

			case "lph":
				return matchesLPPerHour(item, operator, valueStr);

			case "fep":
			case "food":
				return matchesFood(item, fepType, operator, valueStr);

			default:
				return false;
		}
	}

	/**
	 * Matches items with Learning Points (Curiosity)
	 */
	private static boolean matchesLearningPoints(GItem item, String operator, String valueStr) {
		try {
			Curiosity curiosity = ItemInfo.find(Curiosity.class, item.info());
			if (curiosity == null) {
				return false;
			}

			// If no value specified, just check if item has LP
			if (valueStr == null || valueStr.isEmpty()) {
				return curiosity.exp > 0;
			}

			double value = Double.parseDouble(valueStr);
			return compareValue(curiosity.exp, operator, value);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Matches items by LP per hour
	 */
	private static boolean matchesLPPerHour(GItem item, String operator, String valueStr) {
		try {
			Curiosity curiosity = ItemInfo.find(Curiosity.class, item.info());
			if (curiosity == null) {
				return false;
			}

			// If no value specified, just check if item has LP/H
			if (valueStr == null || valueStr.isEmpty()) {
				return curiosity.lph > 0;
			}

			double value = Double.parseDouble(valueStr);
			return compareValue(curiosity.lph, operator, value);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Matches food items with FEP
	 * @param fepType Optional FEP type filter (e.g., "str", "agi", "int") - fuzzy matches event names
	 * @param operator Optional comparison operator (>, <, >=, etc.)
	 * @param valueStr Optional numeric value to compare against
	 */
	private static boolean matchesFood(GItem item, String fepType, String operator, String valueStr) {
		try {
			FoodInfo foodInfo = ItemInfo.find(FoodInfo.class, item.info());
			if (foodInfo == null) {
				return false;
			}

			// Case 1: FEP type filter specified (e.g., fep:str, fep:agi)
			if (fepType != null && !fepType.isEmpty()) {
				// Check if any FEP event matches the type
				for (FoodInfo.Event ev : foodInfo.evs) {
					if (ev.ev != null && ev.ev.nm != null) {
						String eventName = ev.ev.nm.toLowerCase();
						// Use fuzzy matching: "str" matches "Strength", "agi" matches "Agility", etc.
						if (Fuzzy.fuzzyContains(eventName, fepType.toLowerCase())) {
							return true;
						}
					}
				}
				return false;
			}

			// Case 2: No type filter, check numeric value if specified
			if (valueStr == null || valueStr.isEmpty()) {
				// No filters at all, just check if item is food
				return true;
			}

			// Calculate total FEP from events
			double totalFep = 0;
			for (FoodInfo.Event ev : foodInfo.evs) {
				totalFep += ev.a;
			}

			double value = Double.parseDouble(valueStr);
			return compareValue(totalFep, operator, value);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Compares a value against a target using the given operator
	 */
	private static boolean compareValue(double itemValue, String operator, double targetValue) {
		if (operator == null || operator.isEmpty() || operator.equals("=")) {
			// Exact match (with small epsilon for floating point)
			return Math.abs(itemValue - targetValue) < 0.01;
		}

		switch (operator) {
			case ">":
				return itemValue > targetValue;
			case ">=":
				return itemValue >= targetValue;
			case "<":
				return itemValue < targetValue;
			case "<=":
				return itemValue <= targetValue;
			default:
				return Math.abs(itemValue - targetValue) < 0.01;
		}
	}

	/**
	 * Matches item name using fuzzy matching (backward compatible)
	 */
	private static boolean matchesName(GItem item, String searchQuery) {
		try {
			String itemName = item.getname().toLowerCase();
			return Fuzzy.fuzzyContains(itemName, searchQuery);
		} catch (Exception e) {
			return false;
		}
	}
}
