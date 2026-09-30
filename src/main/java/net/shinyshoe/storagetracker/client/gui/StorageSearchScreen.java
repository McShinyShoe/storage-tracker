package net.shinyshoe.storagetracker.client.gui;

import net.shinyshoe.storagetracker.StorageTracker;
import net.shinyshoe.storagetracker.client.render.WorldOverlays;
import net.shinyshoe.storagetracker.config.ModConfig;
import net.shinyshoe.storagetracker.storage.StorageDatabase;
import net.shinyshoe.storagetracker.storage.StorageType;
import net.shinyshoe.storagetracker.storage.TrackedStorage;
import net.shinyshoe.storagetracker.util.SearchTextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
//? if > 1.19.2 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class StorageSearchScreen extends Screen {

	private static final int MARGIN = 8;
	private static final int ROW_HEIGHT = 20;
	private static final int ICON_SIZE = 16;
	private static final int HEADER_BUTTON_WIDTH = 110;

	private enum SortMode {
		NAME("gui.storage-tracker.sort.name"),
		COUNT("gui.storage-tracker.sort.count"),
		NEAREST("gui.storage-tracker.sort.nearest");

		final String labelKey;

		SortMode(String labelKey) {
			this.labelKey = labelKey;
		}

		SortMode next() {
			SortMode[] values = values();
			return values[(ordinal() + 1) % values.length];
		}
	}

	private enum TypeFilter {
		ALL("gui.storage-tracker.type_filter.all", null),
		CHESTS("gui.storage-tracker.type_filter.chests",
			Set.of(StorageType.CHEST, StorageType.TRAPPED_CHEST, StorageType.ENDER_CHEST, StorageType.COPPER_CHEST)),
		BARRELS("gui.storage-tracker.type_filter.barrels", Set.of(StorageType.BARREL, StorageType.SHULKER_BOX)),
		FURNACES("gui.storage-tracker.type_filter.furnaces",
			Set.of(StorageType.FURNACE, StorageType.BLAST_FURNACE, StorageType.SMOKER)),
		OTHER("gui.storage-tracker.type_filter.other", null);

		final String labelKey;
		final Set<StorageType> types;

		TypeFilter(String labelKey, Set<StorageType> types) {
			this.labelKey = labelKey;
			this.types = types;
		}

		TypeFilter next() {
			TypeFilter[] values = values();
			return values[(ordinal() + 1) % values.length];
		}

		boolean matches(StorageType type) {
			return switch (this) {
				case ALL -> true;
				case OTHER -> !CHESTS.types.contains(type) && !BARRELS.types.contains(type) && !FURNACES.types.contains(type);
				default -> types.contains(type);
			};
		}
	}

	private EditBox searchBox;
	private List<ItemEntry> entries = List.of();
	private List<ContainerRow> containerRows = List.of();
	private int selectedItemId = -1;
	private SortMode sortMode = SortMode.NAME;
	private boolean sortAscending = false;
	private TypeFilter typeFilter = TypeFilter.ALL;
	private boolean allDimensions = false;
	private double listScroll;

	private int listX, listY, listW, listBottom;
	private int detailX, detailY, detailW, detailBottom;
	private int typeButtonX, sortButtonX, dimButtonX, headerButtonY;
	private int detailRowsTop;

	public StorageSearchScreen() {
		super(Component.translatable("gui.storage-tracker.search.title"));
	}

	@Override
	protected void init() {
		headerButtonY = MARGIN;
		dimButtonX = width - MARGIN - HEADER_BUTTON_WIDTH;
		sortButtonX = dimButtonX - MARGIN - HEADER_BUTTON_WIDTH;
		typeButtonX = sortButtonX - MARGIN - HEADER_BUTTON_WIDTH;
		int searchWidth = Math.max(100, typeButtonX - MARGIN * 2 - MARGIN);

		searchBox = new EditBox(this.font, MARGIN, MARGIN, searchWidth, ROW_HEIGHT, Component.translatable("gui.storage-tracker.search.box"));
		searchBox.setMaxLength(256);
		searchBox.setResponder(query -> refreshEntries());
		setFocused(searchBox);
		addRenderableWidget(searchBox);

		int contentTop = MARGIN + ROW_HEIGHT + MARGIN;
		int contentBottom = height - MARGIN;
		listX = MARGIN;
		listY = contentTop;
		listW = (int) ((width - MARGIN * 3) * 0.4);
		listBottom = contentBottom;
		detailX = listX + listW + MARGIN;
		detailY = contentTop;
		detailW = width - MARGIN - detailX;
		detailBottom = contentBottom;

		refreshEntries();
	}

	private static void setFocused(EditBox box) {
		//? if > 1.19.2 {
		box.setFocused(true);
		//?} else {
		/*box.setFocus(true);
		*///?}
	}

	private void refreshEntries() {
		StorageDatabase db = StorageTracker.database();
		Object registries = StorageTracker.clientRegistries();
		Minecraft client = Minecraft.getInstance();
		ResourceKey<Level> currentDim = client.level != null ? client.level.dimension() : null;
		Vec3 playerPos = client.player != null ? client.player.position() : Vec3.ZERO;
		String query = searchBox != null ? searchBox.getValue() : "";
		ModConfig config = StorageTracker.config();
		int radiusBlocks = config.searchRadiusBlocks();
		double radiusSq = (double) radiusBlocks * radiusBlocks;
		boolean groupByContainer = config.groupByContainer();

		List<ItemEntry> result = new ArrayList<>();
		for (int itemId : db.items().ids()) {
			String searchText = db.items().searchTextOf(itemId).orElse("");
			if (!SearchTextUtil.matches(query, searchText)) continue;

			ItemStack display = null;
			long total = 0;
			int containerCount = 0;
			double nearestSq = Double.MAX_VALUE;

			for (GlobalPos pos : db.contents().containersOf(itemId)) {
				TrackedStorage storage = db.storages().get(pos).orElse(null);
				if (storage == null || !typeFilter.matches(storage.type())) continue;
				boolean inScope = storage.virtual() || allDimensions
					|| (currentDim != null && pos.dimension().equals(currentDim));
				if (!inScope) continue;

				int count = db.contents().contentsOf(pos).get(itemId);
				if (count <= 0) continue;

				boolean sameDimAsPlayer = !storage.virtual() && currentDim != null && pos.dimension().equals(currentDim);
				double distSq = sameDimAsPlayer ? playerPos.distanceToSqr(
					pos.pos().getX() + 0.5, pos.pos().getY() + 0.5, pos.pos().getZ() + 0.5) : Double.MAX_VALUE;
				if (radiusBlocks > 0 && sameDimAsPlayer && distSq > radiusSq) continue;

				if (display == null) display = db.decodeItem(itemId, registries);
				total += count;
				containerCount++;
				nearestSq = Math.min(nearestSq, distSq);
				if (groupByContainer) {
					result.add(new ItemEntry(itemId, display, count, 1, distSq, pos));
				}
			}
			if (containerCount == 0 || groupByContainer) continue; // per-container rows already added above

			result.add(new ItemEntry(itemId, display, total, containerCount, nearestSq, null));
		}

		Comparator<ItemEntry> comparator = switch (sortMode) {
			case NAME -> Comparator.comparing(e -> e.displayStack().getHoverName().getString(), String.CASE_INSENSITIVE_ORDER);
			case COUNT -> Comparator.comparingLong(ItemEntry::totalCount).reversed();
			case NEAREST -> Comparator.comparingDouble(ItemEntry::nearestDistanceSq);
		};
		result.sort(sortAscending ? comparator.reversed() : comparator);
		entries = result;

		if (entries.stream().noneMatch(e -> e.itemId() == selectedItemId)) {
			selectedItemId = -1;
			containerRows = List.of();
		}
		clampScroll();
	}

	private void refreshContainerRows() {
		if (selectedItemId < 0) {
			containerRows = List.of();
			return;
		}
		StorageDatabase db = StorageTracker.database();
		Minecraft client = Minecraft.getInstance();
		ResourceKey<Level> currentDim = client.level != null ? client.level.dimension() : null;
		Vec3 playerPos = client.player != null ? client.player.position() : null;

		List<ContainerRow> rows = new ArrayList<>();
		for (GlobalPos pos : db.contents().containersOf(selectedItemId)) {
			TrackedStorage storage = db.storages().get(pos).orElse(null);
			if (storage == null || !typeFilter.matches(storage.type())) continue;
			boolean inScope = storage.virtual() || allDimensions
				|| (currentDim != null && pos.dimension().equals(currentDim));
			if (!inScope) continue;

			int count = db.contents().contentsOf(pos).get(selectedItemId);
			if (count <= 0) continue;

			Double distance = null;
			if (!storage.virtual() && playerPos != null && currentDim != null && pos.dimension().equals(currentDim)) {
				distance = Math.sqrt(playerPos.distanceToSqr(
					pos.pos().getX() + 0.5, pos.pos().getY() + 0.5, pos.pos().getZ() + 0.5));
			}
			rows.add(new ContainerRow(storage, count, distance));
		}
		rows.sort(Comparator.comparingDouble(r -> r.distance() == null ? Double.MAX_VALUE : r.distance()));
		containerRows = rows;
	}

	private void clampScroll() {
		int visible = listBottom - listY;
		double maxScroll = Math.max(0, entries.size() * (double) ROW_HEIGHT - visible);
		listScroll = Math.min(Math.max(0, listScroll), maxScroll);
	}

	//? if > 1.19.2 {
	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		RenderContext ctx = new RenderContext(graphics);
		renderContent(ctx, mouseX, mouseY, partialTick);
		searchBox.render(graphics, mouseX, mouseY, partialTick);
		findHoveredItem(mouseX, mouseY).ifPresent(stack -> {
			//? if >= 1.21.7 {
			graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
			//?} else {
			/*graphics.renderTooltip(font, stack, mouseX, mouseY);
			*///?}
		});
	}
	//?} else {
	/*@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
		RenderContext ctx = new RenderContext(pose);
		renderContent(ctx, mouseX, mouseY, partialTick);
		searchBox.render(pose, mouseX, mouseY, partialTick);
		findHoveredItem(mouseX, mouseY).ifPresent(stack -> this.renderTooltip(pose, stack, mouseX, mouseY));
	}
	*///?}

	private void renderContent(RenderContext ctx, int mouseX, int mouseY, float partialTick) {
		//? if >= 1.21.7 {
		//?} else {
		/*ctx.renderBackground(this, mouseX, mouseY, partialTick);
		*///?}
		renderHeaderButtons(ctx, mouseX, mouseY);
		renderList(ctx, mouseX, mouseY);
		renderDetailPanel(ctx, mouseX, mouseY);
		renderStatusLine(ctx);
	}

	private void renderHeaderButtons(RenderContext ctx, int mouseX, int mouseY) {
		boolean typeHover = hitTest(typeButtonX, headerButtonY, HEADER_BUTTON_WIDTH, ROW_HEIGHT, mouseX, mouseY);
		boolean sortHover = hitTest(sortButtonX, headerButtonY, HEADER_BUTTON_WIDTH, ROW_HEIGHT, mouseX, mouseY);
		boolean dimHover = hitTest(dimButtonX, headerButtonY, HEADER_BUTTON_WIDTH, ROW_HEIGHT, mouseX, mouseY);

		ctx.fill(typeButtonX, headerButtonY, typeButtonX + HEADER_BUTTON_WIDTH, headerButtonY + ROW_HEIGHT, typeHover ? 0xFF555555 : 0xFF333333);
		ctx.fill(sortButtonX, headerButtonY, sortButtonX + HEADER_BUTTON_WIDTH, headerButtonY + ROW_HEIGHT, sortHover ? 0xFF555555 : 0xFF333333);
		ctx.fill(dimButtonX, headerButtonY, dimButtonX + HEADER_BUTTON_WIDTH, headerButtonY + ROW_HEIGHT, dimHover ? 0xFF555555 : 0xFF333333);

		Component typeLabel = Component.translatable(typeFilter.labelKey);
		Component sortLabel = Component.translatable("gui.storage-tracker.sort_label",
			Component.translatable(sortMode.labelKey), sortAscending ? "^" : "v");
		Component dimLabel = Component.translatable(allDimensions ? "gui.storage-tracker.dimension.all" : "gui.storage-tracker.dimension.current");
		ctx.drawText(font, typeLabel, typeButtonX + 4, headerButtonY + 6, 0xFFFFFFFF);
		ctx.drawText(font, sortLabel, sortButtonX + 4, headerButtonY + 6, 0xFFFFFFFF);
		ctx.drawText(font, dimLabel, dimButtonX + 4, headerButtonY + 6, 0xFFFFFFFF);
	}

	private void renderList(RenderContext ctx, int mouseX, int mouseY) {
		ctx.fill(listX, listY, listX + listW, listBottom, 0x66000000);
		ctx.enableScissor(listX, listY, listX + listW, listBottom);

		int y = listY - (int) listScroll;
		for (ItemEntry entry : entries) {
			if (y + ROW_HEIGHT >= listY && y <= listBottom) {
				boolean hovered = mouseX >= listX && mouseX < listX + listW && mouseY >= y && mouseY < y + ROW_HEIGHT
					&& mouseY >= listY && mouseY < listBottom;
				boolean selected = entry.itemId() == selectedItemId;
				if (selected) {
					ctx.fill(listX, y, listX + listW, y + ROW_HEIGHT, 0xFF4B6EAF);
				} else if (hovered) {
					ctx.fill(listX, y, listX + listW, y + ROW_HEIGHT, 0xFF3A3A3A);
				}

				ctx.drawItem(font, entry.displayStack(), listX + 2, y + 2);
				String name = entry.displayStack().getHoverName().getString();
				if (entry.containerPos() != null) {
					name = name + " @ " + containerLabel(entry.containerPos());
				}
				ctx.drawText(font, trimToWidth(name, listW - ICON_SIZE - 50), listX + ICON_SIZE + 6, y + 6, 0xFFFFFFFF);
				String countText = "x" + entry.totalCount();
				ctx.drawText(font, countText, listX + listW - font.width(countText) - 4, y + 6, 0xFFAAAAAA);
			}
			y += ROW_HEIGHT;
		}

		ctx.disableScissor();
	}

	private void renderDetailPanel(RenderContext ctx, int mouseX, int mouseY) {
		ctx.fill(detailX, detailY, detailX + detailW, detailBottom, 0x66000000);
		if (selectedItemId < 0) {
			ctx.drawText(font, Component.translatable("gui.storage-tracker.detail.none"), detailX + 6, detailY + 6, 0xFFAAAAAA);
			detailRowsTop = detailBottom;
			return;
		}

		ItemEntry selected = entries.stream().filter(e -> e.itemId() == selectedItemId).findFirst().orElse(null);
		int y = detailY + 4;
		if (selected != null) {
			ctx.drawText(font, selected.displayStack().getHoverName(), detailX + 6, y, 0xFFFFFFFF);
			y += ROW_HEIGHT;
			Component summary = Component.translatable("gui.storage-tracker.detail.summary", selected.totalCount(), selected.containerCount());
			ctx.drawText(font, summary, detailX + 6, y, 0xFFAAAAAA);
			y += ROW_HEIGHT;
		}

		int rowsTop = y;
		detailRowsTop = rowsTop;
		ctx.enableScissor(detailX, rowsTop, detailX + detailW, detailBottom);
		for (ContainerRow row : containerRows) {
			if (y + ROW_HEIGHT * 2 < rowsTop || y > detailBottom) {
				y += ROW_HEIGHT * 2;
				continue;
			}
			boolean hovered = mouseX >= detailX && mouseX < detailX + detailW && mouseY >= y && mouseY < y + ROW_HEIGHT * 2
				&& mouseY >= rowsTop && mouseY < detailBottom;
			if (hovered) {
				ctx.fill(detailX, y, detailX + detailW, y + ROW_HEIGHT * 2, 0xFF3A3A3A);
			}

			ctx.drawText(font, storageDisplayName(row.storage()), detailX + 6, y + 2, 0xFFFFFFFF);
			String locationLine = row.storage().virtual()
				? Component.translatable("gui.storage-tracker.detail.everywhere").getString()
				: "%s  [%d, %d, %d]  x%d".formatted(
				row.storage().pos().dimension().location().getPath(),
				row.storage().pos().pos().getX(), row.storage().pos().pos().getY(), row.storage().pos().pos().getZ(),
				row.count());
			ctx.drawText(font, locationLine, detailX + 6, y + 2 + font.lineHeight, 0xFFAAAAAA);
			if (row.distance() != null) {
				String distText = "%.0fm".formatted(row.distance());
				ctx.drawText(font, distText, detailX + detailW - font.width(distText) - 4, y + 2, 0xFF77CC77);
			}
			y += ROW_HEIGHT * 2;
		}
		ctx.disableScissor();
	}

	private void renderStatusLine(RenderContext ctx) {
		StorageDatabase db = StorageTracker.database();
		Component status = Component.translatable("gui.storage-tracker.status",
			db.items().size(), db.storages().getAll().size());
		ctx.drawText(font, status, MARGIN, height - MARGIN - font.lineHeight, 0xFF777777);
	}

	private Optional<ItemStack> findHoveredItem(int mouseX, int mouseY) {
		if (mouseX < listX || mouseX >= listX + listW || mouseY < listY || mouseY >= listBottom) return Optional.empty();
		int index = rowIndexAt(mouseY);
		if (index < 0 || index >= entries.size()) return Optional.empty();
		return Optional.of(entries.get(index).displayStack());
	}

	private int rowIndexAt(double mouseY) {
		double relative = mouseY - listY + listScroll;
		if (relative < 0) return -1;
		return (int) (relative / ROW_HEIGHT);
	}

	private static boolean hitTest(int x, int y, int w, int h, double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
	}

	private String containerLabel(GlobalPos pos) {
		TrackedStorage storage = StorageTracker.database().storages().get(pos).orElse(null);
		if (storage == null) return "";
		if (storage.customName() != null) return storage.customName();
		return "[%d, %d, %d]".formatted(pos.pos().getX(), pos.pos().getY(), pos.pos().getZ());
	}

	private String storageDisplayName(TrackedStorage storage) {
		if (storage.customName() != null) return storage.customName();
		var id = storage.type().getId();
		return Component.translatable("block." + id.getNamespace() + "." + id.getPath()).getString();
	}

	private String trimToWidth(String text, int maxWidth) {
		if (font.width(text) <= maxWidth) return text;
		return font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("..."))) + "...";
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (hitTest(typeButtonX, headerButtonY, HEADER_BUTTON_WIDTH, ROW_HEIGHT, mouseX, mouseY)) {
			typeFilter = typeFilter.next();
			refreshEntries();
			refreshContainerRows();
			return true;
		}
		if (hitTest(sortButtonX, headerButtonY, HEADER_BUTTON_WIDTH, ROW_HEIGHT, mouseX, mouseY)) {
			if (button == 1) {
				sortAscending = !sortAscending;
			} else {
				sortMode = sortMode.next();
			}
			refreshEntries();
			return true;
		}
		if (hitTest(dimButtonX, headerButtonY, HEADER_BUTTON_WIDTH, ROW_HEIGHT, mouseX, mouseY)) {
			allDimensions = !allDimensions;
			refreshEntries();
			refreshContainerRows();
			return true;
		}
		if (mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listBottom) {
			int index = rowIndexAt(mouseY);
			if (index >= 0 && index < entries.size()) {
				selectedItemId = entries.get(index).itemId();
				refreshContainerRows();
				return true;
			}
		}
		if (mouseX >= detailX && mouseX < detailX + detailW && mouseY >= detailRowsTop && mouseY < detailBottom) {
			int index = (int) ((mouseY - detailRowsTop) / (ROW_HEIGHT * 2));
			if (index >= 0 && index < containerRows.size()) {
				locate(containerRows.get(index).storage());
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void locate(TrackedStorage storage) {
		var player = Minecraft.getInstance().player;
		if (player == null) return;

		Component message = storage.virtual()
			? Component.translatable("gui.storage-tracker.detail.everywhere")
			: Component.translatable("message.storage-tracker.locate",
			storage.pos().dimension().location().getPath(),
			storage.pos().pos().getX(), storage.pos().pos().getY(), storage.pos().pos().getZ());
		player.displayClientMessage(message, false);

		if (!storage.virtual()) {
			long durationMillis = StorageTracker.config().highlightDurationSeconds() * 1000L;
			WorldOverlays.highlights().highlight(storage.pos(), System.currentTimeMillis(), durationMillis);
		}
	}

	//? if > 1.19.2 {
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return handleScroll(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
	//?} else {
	/*@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
		return handleScroll(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollY);
	}
	*///?}

	private boolean handleScroll(double mouseX, double mouseY, double scrollY) {
		if (mouseX < listX || mouseX >= listX + listW || mouseY < listY || mouseY >= listBottom) return false;
		listScroll -= scrollY * ROW_HEIGHT * 2;
		clampScroll();
		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
