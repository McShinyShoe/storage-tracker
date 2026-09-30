package net.shinyshoe.storagetracker.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.shinyshoe.storagetracker.StorageTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class SearchTextUtil {
	private static final Pattern FORMATTING_CODE = Pattern.compile("(?i)§[0-9A-FK-OR]");

	private SearchTextUtil() {
	}

	// Strip the § formatting
	private static String stripFormatting(String raw) {
		if (raw == null || raw.isEmpty()) return "";
		return FORMATTING_CODE.matcher(raw).replaceAll("");
	}

	// Make the string normal
	private static String normalize(String raw) {
		return stripFormatting(raw).toLowerCase(Locale.ROOT);
	}

	private static String mergeTexts(String title, String lore, List<String> extraTokens) {
		StringBuilder sb = new StringBuilder(normalize(title));
		if (lore != null && !lore.isBlank()) {
			sb.append(' ').append(normalize(lore));
		}
		if (extraTokens != null) {
			for (String token : extraTokens) {
				if (token != null && !token.isBlank()) {
					sb.append(' ').append(token.toLowerCase(Locale.ROOT));
				}
			}
		}
		return sb.toString();
	}

	public static boolean matches(String query, String precomputedSearchText) {
		if (query == null || query.isBlank()) return true;
		if (precomputedSearchText == null) return false;
		return precomputedSearchText.contains(normalize(query));
	}

	private static List<String> getLoreLines(ItemStack stack) {
		List<String> lines = new ArrayList<>();
		try {
			//? if > 1.19.2 {
			net.minecraft.world.item.component.ItemLore lore = stack.get(net.minecraft.core.component.DataComponents.LORE);
			if (lore != null) {
				for (Component line : lore.lines()) lines.add(line.getString());
			}
			//?} else {
			/*net.minecraft.nbt.CompoundTag display = stack.getTagElement("display");
			if (display != null && display.contains("Lore", 9)) {
				net.minecraft.nbt.ListTag loreList = display.getList("Lore", 8);
				for (int i = 0; i < loreList.size(); i++) {
					Component line = Component.Serializer.fromJson(loreList.getString(i));
					if (line != null) lines.add(line.getString());
				}
			}
			*///?}
		} catch (RuntimeException e) {
			// Malformed lore on a foreign/modded stack must never crash tracking (FR-1.8).
			StorageTracker.LOGGER.debug("Failed to read lore for {}", stack, e);
		}
		return lines;
	}
	private static List<String> getEnchantmentLines(ItemStack stack) {
		List<String> ids = new ArrayList<>();
		try {
			//? if > 1.19.2 {
			net.minecraft.world.item.enchantment.ItemEnchantments enchantments =
					stack.get(net.minecraft.core.component.DataComponents.ENCHANTMENTS);
			if (enchantments != null) {
				for (var entry : enchantments.entrySet()) {
					entry.getKey().unwrapKey().ifPresent(key -> ids.add(key.location().getPath()));
				}
			}
			//?} else {
			/*var enchantments = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(stack);
			for (var enchantment : enchantments.keySet()) {
				net.minecraft.resources.ResourceLocation key = net.minecraft.core.Registry.ENCHANTMENT.getKey(enchantment);
				if (key != null) ids.add(key.getPath());
			}
			*///?}
		} catch (RuntimeException e) {
			StorageTracker.LOGGER.debug("Failed to read enchantments for {}", stack, e);
		}
		return ids;
	}

	public static String build(ItemStack stack) {
		String title = stack.getHoverName().getString();
		String lore = String.join(" ", getLoreLines(stack));

		List<String> tokens = new ArrayList<>();
		ResourceLocation id = RegistryUtil.itemId(stack.getItem());
		if (id != null) {
			tokens.add("id:" + id);
			tokens.add("id:" + id.getPath());
		}
		for (String enchantId : getEnchantmentLines(stack)) {
			tokens.add("enchant:" + enchantId);
		}

		return mergeTexts(title, lore, tokens);
	}
}
