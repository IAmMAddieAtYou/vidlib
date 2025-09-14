package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.math.KMath;
import imgui.ImGui;

public class MemoryUsagePanel extends AdminPanel {
	public static final MemoryUsagePanel INSTANCE = new MemoryUsagePanel();

	private static long toMB(long bytes) {
		return bytes / 1000L / 1000L;
	}

	private MemoryUsagePanel() {
		super("memory-usage", "Memory Usage");
		this.style = AdminPanelStyle.MINIMAL;
	}

	@Override
	public void content(ImGraphics graphics) {
		long maxMemory = Runtime.getRuntime().maxMemory();
		long totalMemory = Runtime.getRuntime().totalMemory();
		long freeMemory = Runtime.getRuntime().freeMemory();
		long usedMemory = totalMemory - freeMemory;

		ImGui.pushItemWidth(-1F);

		ImGui.text("Memory: %2d%% %,03d/%,03d MB".formatted(usedMemory * 100L / totalMemory, toMB(usedMemory), toMB(totalMemory)));
		ImGui.progressBar(KMath.clamp(usedMemory / (float) totalMemory, 0F, 1F), 0F, 20F, "");

		ImGui.text("Allocated: %2d%% %,03d/%,03d MB".formatted(totalMemory * 100L / maxMemory, toMB(totalMemory), toMB(maxMemory)));
		ImGui.progressBar(KMath.clamp(totalMemory / (float) maxMemory, 0F, 1F), 0F, 20F, "");

		ImGui.popItemWidth();
	}
}
