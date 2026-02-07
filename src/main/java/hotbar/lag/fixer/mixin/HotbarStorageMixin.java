package hotbar.lag.fixer.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.nbt.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;


@Mixin(HotbarStorage.class)
public abstract class HotbarStorageMixin {
	@Shadow private HotbarStorageEntry[] entries;
	@Shadow private Path file;

    @Shadow private boolean loaded;
    @Shadow private DataFixer dataFixer;


    @Inject(method = "<init>", at = @At("TAIL"))
    private void preload(Path directory, DataFixer dataFixer, CallbackInfo ci) {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            client.getCreativeHotbarStorage().getSavedHotbar(0);
        });
    }

	@Inject(method = "save", at = @At("HEAD"), cancellable = true)
	private void saveAsync(CallbackInfo ci) {
		ci.cancel();
		CompletableFuture.runAsync(() -> {
			try {
				NbtCompound nbtCompound = NbtHelper.putDataVersion(new NbtCompound());

				for (int i = 0; i < 9; ++i) {
					HotbarStorageEntry entry = this.entries[i];
					DataResult<NbtElement> result = HotbarStorageEntry.CODEC.encodeStart(NbtOps.INSTANCE, entry);
					nbtCompound.put(String.valueOf(i), result.getOrThrow());
				}

				NbtIo.write(nbtCompound, this.file);
			} catch (Exception e) {
				LogUtils.getLogger().error("Async hotbar save failed", e);
			}
		});
	}

}

