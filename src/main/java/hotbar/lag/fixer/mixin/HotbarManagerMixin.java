package hotbar.lag.fixer.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.nbt.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;


@Mixin(HotbarManager.class)
public abstract class HotbarManagerMixin {
	@Shadow private Hotbar[] hotbars;
	@Shadow private Path optionsFile;

    @Shadow private boolean loaded;
    @Shadow private DataFixer fixerUpper;


    @Inject(method = "<init>", at = @At("TAIL"))
    private void preload(Path directory, DataFixer dataFixer, CallbackInfo ci) {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            client.getHotbarManager().get(0);
        });
    }

	@Inject(method = "save", at = @At("HEAD"), cancellable = true)
	private void saveAsync(CallbackInfo ci) {
		ci.cancel();
		CompletableFuture.runAsync(() -> {
			try {
				CompoundTag nbtCompound = NbtUtils.addCurrentDataVersion(new CompoundTag());

				for (int i = 0; i < 9; ++i) {
					Hotbar entry = this.hotbars[i];
					DataResult<Tag> result = Hotbar.CODEC.encodeStart(NbtOps.INSTANCE, entry);
					nbtCompound.put(String.valueOf(i), result.getOrThrow());
				}

				NbtIo.write(nbtCompound, this.optionsFile);
			} catch (Exception e) {
				LogUtils.getLogger().error("Async hotbar save failed", e);
			}
		});
	}

}
