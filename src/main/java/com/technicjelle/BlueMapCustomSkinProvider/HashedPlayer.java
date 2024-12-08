package com.technicjelle.BlueMapCustomSkinProvider;

import de.bluecolored.bluemap.common.serverinterface.Player;

//TODO: Remove them BlueMap 5.6 is out!
// I have PR'd these two functions to BlueMap's own Player class
// https://github.com/BlueMap-Minecraft/BlueMap/pull/629
public record HashedPlayer(Player player) {
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		HashedPlayer other = (HashedPlayer) o;
		return player.getUuid().equals(other.player.getUuid());
	}

	@Override
	public int hashCode() {
		return player.getUuid().hashCode();
	}
}
