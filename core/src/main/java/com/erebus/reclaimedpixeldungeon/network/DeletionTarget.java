package com.erebus.reclaimedpixeldungeon.network;

/** Binds an acknowledged deletion to one slot and one persisted character identity. */
public final class DeletionTarget {
    public final int slot;
    private final String characterId;
    public DeletionTarget( int slot, String characterId ) { this.slot = slot; this.characterId = characterId; }
    public boolean matches( int actualSlot, String actualId ) {
        return slot > 0 && slot == actualSlot && characterId != null && !characterId.isEmpty() && characterId.equals( actualId );
    }
}
