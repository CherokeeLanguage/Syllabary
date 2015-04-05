
Game Loop:

ACT:
	Is deck loaded? (no) LOAD, EXIT
	
	Is ANY DIALOG showing? (yes), EXIT
	
	Is time up? (yes) DO END-SESSION, EXIT
	
	Is GAMEBOARD active? (yes) Update Timers, EXIT
	
	Cards left in current stack? (no) DO LOAD-STACK (with DISCARDS and NEW CARDS), EXIT
	
	Pull card from stack as CARD.
	
	Is CARD new? (yes) DO NEW CARD DIALOG WITH CARD, RE-INSERT CARD INTO STACK, EXIT
	
	Load GAMEBOARD with CARD.
	
	Place CARD into DISCARDS.
	
	Make GAMEBOARD active.
	
	EXIT
	
	
	