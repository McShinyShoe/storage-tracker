package net.shinyshoe.storagetracker.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
//? if < 26.1.2 {
import net.minecraft.client.renderer.RenderType;
//?} else {
/*import net.minecraft.client.renderer.rendertype.RenderTypes;
*///?}
//? if > 1.19.2 {
import org.joml.Matrix4f;
//?} else {
/*import com.mojang.math.Matrix4f;
*///?}

public final class WorldOverlayRenderer {

	private WorldOverlayRenderer() {
	}

	public static void renderBoxOutline(PoseStack pose, MultiBufferSource buffers, Vec3 cameraPos, AABB box, float r, float g, float b, float a) {
		//? if < 26.1.2 {
		var consumer = buffers.getBuffer(RenderType.lines());
		//?} else {
		/*var consumer = buffers.getBuffer(RenderTypes.lines());
		*///?}
		var matrix = pose.last().pose();

		float minX = (float) (box.minX - cameraPos.x);
		float minY = (float) (box.minY - cameraPos.y);
		float minZ = (float) (box.minZ - cameraPos.z);
		float maxX = (float) (box.maxX - cameraPos.x);
		float maxY = (float) (box.maxY - cameraPos.y);
		float maxZ = (float) (box.maxZ - cameraPos.z);

		// Bottom face, top face, then the four vertical edges connecting them: 12 edges total.
		edge(consumer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
		edge(consumer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
		edge(consumer, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
		edge(consumer, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

		edge(consumer, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
		edge(consumer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
		edge(consumer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
		edge(consumer, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

		edge(consumer, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
		edge(consumer, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
		edge(consumer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
		edge(consumer, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
	}

	//? if > 1.19.2 {
	private static void edge(VertexConsumer consumer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
		float[] n = normal(x1, y1, z1, x2, y2, z2);
		consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(n[0], n[1], n[2]);
		consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(n[0], n[1], n[2]);
	}
	//?} else {
	/*private static void edge(VertexConsumer consumer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
		float[] n = normal(x1, y1, z1, x2, y2, z2);
		consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(n[0], n[1], n[2]).endVertex();
		consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(n[0], n[1], n[2]).endVertex();
	}
	*///?}

	private static float[] normal(float x1, float y1, float z1, float x2, float y2, float z2) {
		float nx = x2 - x1, ny = y2 - y1, nz = z2 - z1;
		float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
		return len > 0 ? new float[]{nx / len, ny / len, nz / len} : new float[]{0, 1, 0};
	}

	public static void renderBillboardText(PoseStack pose, MultiBufferSource buffers, Font font, Component text, Vec3 worldPos, Vec3 cameraPos, Camera camera, float scale, int color) {
		pose.pushPose();
		pose.translate(worldPos.x - cameraPos.x, worldPos.y - cameraPos.y, worldPos.z - cameraPos.z);
		pose.mulPose(camera.rotation());
		pose.scale(-scale, -scale, scale);

		var matrix = pose.last().pose();
		float textX = -font.width(text) / 2f;
		int background = 0x40000000;
		//? if > 1.19.2 {
		font.drawInBatch(text, textX, 0, color, false, matrix, buffers, Font.DisplayMode.SEE_THROUGH, background, 0xF000F0);
		//?} else {
		/*font.drawInBatch(text, textX, 0, color, false, matrix, buffers, true, background, 0xF000F0);
		*///?}

		pose.popPose();
	}
}
