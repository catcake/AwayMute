package xyz.catcake.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.jetbrains.annotations.NotNull;

/** This only exists because it's unreasonable to change the l4j config that ships with MC. */
public final class PrefixedMessageFactory implements MessageFactory {

	private final String         messagePrefix;
	private final MessageFactory messageFactory;

	public PrefixedMessageFactory(@NotNull final String messagePrefix) {
		this.messagePrefix = messagePrefix;
		messageFactory     = LogManager.getRootLogger().getMessageFactory();
	}

	private String prefix(final String message) { return String.format("%s %s", messagePrefix, message); }

	@Override
	public Message newMessage(final Object message) { return messageFactory.newMessage(messagePrefix, message); }

	@Override
	public Message newMessage(final String message) { return messageFactory.newMessage(prefix(message)); }

	@Override
	public Message newMessage(final String message, final Object... params)
		{ return messageFactory.newMessage(prefix(message), params); }
}