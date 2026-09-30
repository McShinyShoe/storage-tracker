package net.shinyshoe.storagetracker.client.render;

import net.shinyshoe.storagetracker.StorageTracker;
import net.shinyshoe.storagetracker.config.ModConfig;
import net.shinyshoe.storagetracker.storage.StorageDatabase;
import net.shinyshoe.storagetracker.storage.TrackedStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class WorldOverlays {

	private static final HighlightTracker<GlobalPos> HIGHLIGHTS = new HighlightTracker<>();

	private WorldOverlays() {
	}

	public static HighlightTracker<GlobalPos> highlights() {
		return HIGHLIGHTS;
	}

	public static void render(PoseStack pose, MultiBufferSource buffers, Camera camera) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null || client.player == null) return;

		ResourceKey<Level> currentDim = client.level.dimension();
		//? if < 26.1.2 {
		Vec3 cameraPos = camera.getPosition();
		//?} else {
		/*Vec3 cameraPos = camera.position();
		*///?}
		long now = System.currentTimeMillis();

		for (GlobalPos pos : HIGHLIGHTS.activeKeys(now)) {
			if (!pos.dimension().equals(currentDim)) continue;
			AABB box = new AABB(pos.pos()).inflate(0.02);
			WorldOverlayRenderer.renderBoxOutline(pose, buffers, cameraPos, box, 1.0f, 0.9f, 0.2f, 1.0f);
		}

		ModConfig config = StorageTracker.config();
		if (config.labelMode() == ModConfig.LabelMode.DISABLED) return;

		BlockPos hoveredPos = hoveredBlockPos(client);
		StorageDatabase db = StorageTracker.database();
		for (TrackedStorage storage : List.copyOf(db.storages().getAll())) {
			if (storage.virtual() || storage.customName() == null) continue;
			if (!storage.pos().dimension().equals(currentDim)) continue;

			BlockPos blockPos = storage.pos().pos();
			double distSq = cameraPos.distanceToSqr(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
			boolean hovered = blockPos.equals(hoveredPos);
			if (!NameLabelRenderer.shouldShowLabel(config.labelMode(), hovered, distSq, NameLabelRenderer.MAX_DISTANCE_SQ)) continue;

			Vec3 labelPos = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 1.3, blockPos.getZ() + 0.5);
			WorldOverlayRenderer.renderBillboardText(pose, buffers, client.font,
				Component.literal(storage.customName()), labelPos, cameraPos, camera, 0.025f, 0xFFFFFFFF);
		}
	}

	private static BlockPos hoveredBlockPos(Minecraft client) {
		HitResult hit = client.hitResult;
		return hit instanceof BlockHitResult blockHit ? blockHit.getBlockPos() : null;
	}
}
