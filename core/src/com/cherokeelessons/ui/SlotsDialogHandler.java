package com.cherokeelessons.ui;

public interface SlotsDialogHandler {
	/**
	 * Switch to "play" screen with specified "slot"
	 */
	public void play(int slot);
	/**
	 * Activate slot specific edit dialog.
	 * @param slot
	 */
	public void edit(int slot);
	/**
	 * Erase slot.
	 * @param slot
	 */
	public void erase(int slot);
	/**
	 * Sync slot with cloud service.
	 * @param slot
	 */
	public void sync(int slot);
}