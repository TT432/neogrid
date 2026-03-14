package io.github.tt432.neogrid.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.tt432.neogrid.block.ElectricPoleBlock;
import io.github.tt432.neogrid.block.entity.ElectricPoleBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public class ElectricPoleBlockEntityRenderer implements BlockEntityRenderer<ElectricPoleBlockEntity> {
    public ElectricPoleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ElectricPoleBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        List<BlockPos> connections = blockEntity.getConnectedBlocks();
        
        BlockPos pos = blockEntity.getBlockPos();
        Vec3 startPos = new Vec3(0.5, 1.8, 0.5);

        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f matrix = lastPose.pose();

        // Read wire color from the block
        float wireR = 0.0F, wireG = 0.5F, wireB = 1.0F;
        if (blockEntity.getBlockState().getBlock() instanceof ElectricPoleBlock pole) {
            wireR = pole.getWireColorR();
            wireG = pole.getWireColorG();
            wireB = pole.getWireColorB();
        }

        for (BlockPos target : connections) {
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.leash());
            
            Vec3 targetVec = new Vec3(target.getX(), target.getY(), target.getZ());
            Vec3 beVec = new Vec3(pos.getX(), pos.getY(), pos.getZ());
            
            double targetYOffset = 0.5;
            if (blockEntity.getLevel().getBlockEntity(target) instanceof ElectricPoleBlockEntity) {
                targetYOffset = 1.8;
            }
            
            Vec3 endPos = targetVec.subtract(beVec).add(0.5, targetYOffset, 0.5);

            float xDiff = (float) (endPos.x - startPos.x);
            float yDiff = (float) (endPos.y - startPos.y);
            float zDiff = (float) (endPos.z - startPos.z);
            
            renderLine(consumer, matrix, startPos, xDiff, yDiff, zDiff, LightTexture.FULL_BRIGHT, wireR, wireG, wireB);
        }
    }

    private void renderLine(VertexConsumer buffer, Matrix4f pose, Vec3 startPos, float dx, float dy, float dz, int light, float r, float g, float b) {
        float width = 0.04F;

        float xzLenSq = dx * dx + dz * dz;
        float perpX, perpZ;

        if (xzLenSq < 1e-6) {
            perpX = width;
            perpZ = 0;
        } else {
            float invLen = Mth.invSqrt(xzLenSq) * width;
            perpX = dz * invLen;
            perpZ = dx * invLen;
        }

        float sX = (float) startPos.x;
        float sY = (float) startPos.y;
        float sZ = (float) startPos.z;

        // Top Face
        buffer.addVertex(pose, sX - perpX, sY + width, sZ + perpZ).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, sX + perpX, sY + width, sZ - perpZ).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, sX + dx + perpX, sY + dy + width, sZ + dz - perpZ).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, sX + dx - perpX, sY + dy + width, sZ + dz + perpZ).setColor(r, g, b, 1.0F).setLight(light);

        // Bottom Face
        buffer.addVertex(pose, sX - perpX, sY - width, sZ + perpZ).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, sX + dx - perpX, sY + dy - width, sZ + dz + perpZ).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, sX + dx + perpX, sY + dy - width, sZ + dz - perpZ).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, sX + perpX, sY - width, sZ - perpZ).setColor(r, g, b, 1.0F).setLight(light);

        // Side Face 1
        buffer.addVertex(pose, sX - perpX, sY - width, sZ + perpZ).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, sX - perpX, sY + width, sZ + perpZ).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, sX + dx - perpX, sY + dy + width, sZ + dz + perpZ).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, sX + dx - perpX, sY + dy - width, sZ + dz + perpZ).setColor(r, g, b, 1.0F).setLight(light);

        // Side Face 2
        buffer.addVertex(pose, sX + perpX, sY - width, sZ - perpZ).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, sX + dx + perpX, sY + dy - width, sZ + dz - perpZ).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, sX + dx + perpX, sY + dy + width, sZ + dz - perpZ).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, sX + perpX, sY + width, sZ - perpZ).setColor(r, g, b, 1.0F).setLight(light);
    }

    @Override
    public boolean shouldRenderOffScreen(ElectricPoleBlockEntity blockEntity) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(ElectricPoleBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(blockEntity.getConnectionRange() + 1);
    }
}
