package snownee.jade.gui.config.value;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import snownee.jade.gui.config.OptionsList;

public abstract class OptionValue<T> extends OptionsList.Entry {

	private static final Component SERVER_FEATURE = Component.literal("*").withStyle(ChatFormatting.GRAY);
	protected final Consumer<T> setter;
	private final Component title;
	public boolean serverFeature;
	protected T value;
	protected int indent;
	private int x;

	public OptionValue(String optionName, Consumer<T> setter) {
		this.title = makeTitle(optionName);
		this.setter = setter;
		String key = makeKey(optionName + "_desc");
		if (I18n.exists(key))
			appendDescription(I18n.get(key));
	}

	@Override
	public final void render(GuiGraphics guiGraphics, int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTime) {
		AbstractWidget widget = getListener();
		Component title0 = widget.active ? title : title.copy().withStyle(ChatFormatting.STRIKETHROUGH, ChatFormatting.GRAY);
		int left = rowLeft + indent + 10;
		int top = rowTop + (height / 2) - (client.font.lineHeight / 2);
		guiGraphics.drawString(client.font, title0, left, top, 16777215);
		if (serverFeature) {
			guiGraphics.drawString(client.font, SERVER_FEATURE, left + getTextWidth() + 1, top, 16777215);
		}
		widget.setX(rowLeft + width - 110);
		widget.setY(rowTop + height / 2 - widget.getHeight() / 2);
		drawValue(guiGraphics, width, height, mouseX, mouseY, hovered, deltaTime);
		this.x = rowLeft;
	}

	public void save() {
		setter.accept(value);
	}

	public Component getTitle() {
		return title;
	}

	public void appendDescription(String description) {
		if (this.description == null)
			this.description = description;
		else
			this.description += '\n' + description;
	}

	public int getX() {
		return x;
	}

	@Override
	public int getTextX(int width) {
		return getX() + indent + 10;
	}

	@Override
	public int getTextWidth() {
		return client.font.width(getTitle());
	}

	@Override
	public void updateNarration(NarrationElementOutput output) {
		getListener().updateNarration(output);
		if (!Strings.isNullOrEmpty(getDescription())) {
			output.add(NarratedElementType.HINT, Component.translatable(getDescription()));
		}
	}

	protected abstract void drawValue(GuiGraphics guiGraphics, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean selected, float partialTicks);

	@Override
	public List<? extends AbstractWidget> children() {
		return Lists.newArrayList(getListener());
	}

	public boolean isValidValue() {
		return true;
	}

	@Override
	public void parent(OptionsList.Entry parent) {
		super.parent(parent);
		if (parent instanceof OptionValue) {
			indent = ((OptionValue<?>) parent).indent + 12;
		}
	}

	@Override
	public List<String> getMessages() {
		return List.of(getTitle().getString(), getListener().getMessage().getString());
	}
}
