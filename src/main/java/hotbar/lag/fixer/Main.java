package hotbar.lag.fixer;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("hotbarlagfixer");

	@Override
	public void onInitialize() {
		LOGGER.info("Hotbar lag fixer initialised");
	}
}