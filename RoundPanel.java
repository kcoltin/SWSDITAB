//*****************************************************************************
// RoundPanel.java
// Kevin Coltin
// 
// Panel from which the user can view and manage an individual round.  
//*****************************************************************************




import javax.swing.*; 
import javax.swing.event.*; 
import javax.swing.text.BadLocationException; 
import java.awt.*; 
import java.awt.event.*; 
import java.util.ArrayList; 

class RoundPanel extends JFrame 
{
	private static final Dimension DEFAULT_SIZE = new Dimension(800, 800); 
	
	private Tournament tournament; 
	private TournamentFrame tf; 
	private Round round; 

	private GridBagConstraints gbc; 	
	
	private JScrollPane scrollPane; 
	private RPMenu menu;  
	private JPanel mainPanel; 
	private JPanel topPanel, buttonsBar, bottomPanel; 
	
	//indicates which of the view options at bottom is selected
	private int bottomPanelView; 
	private static final int UNASSIGNED_DEBATERS = 1, UNASSIGNED_JUDGES = 2, 
										UNASSIGNED_ROOMS = 3; 
	private JRadioButton unassignedDebaters, unassignedJudges, unassignedRooms; 
	
	//Item currently selected, which can be moved or swapped 
	private SmartLabel selectedItem; 
	
	//Checkbox indicating whether or not changes being made should be locked 
	private JCheckBox lockChanges; 
	
	//indicates whether this panel needs to be refilled with blank lines (for 
	//debates that still need to be assigned) the next time it's opened. 
	boolean needToRefillBlanks; 
	
	//--------------------------------------------------------------------------
	// Constructor 
	//--------------------------------------------------------------------------
	RoundPanel (Round r, TournamentFrame frame)
	{
		round = r; 
		tournament = round.getTournament(); 
		tf = frame;
		setTitle(tournament.getName()); 

		mainPanel = new JPanel();
		bottomPanelView = UNASSIGNED_DEBATERS; 
		needToRefillBlanks = true; 
		gbc = new GridBagConstraints(); 
		//let components spread out
		gbc.weightx = 1.0; 
		gbc.weighty = 1.0; 
		gbc.fill = GridBagConstraints.BOTH; 
		
		scrollPane = new JScrollPane(mainPanel); 
		getContentPane().add(scrollPane); 
		setSize(DEFAULT_SIZE); 
		setVisible(false);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); 
	}
	
	
	//---------------------------------------------------------------------------
	// Reinitializes the entire screen - should be called whenever changes are
	// made from any part of the program that affect the RoundPanel and whenever
	// the panel is opened after having been invisible.  
	//---------------------------------------------------------------------------
	void initialize (TournamentFrame frame)
	{
		tf = frame;
		
		mainPanel.removeAll(); 
		mainPanel.setLayout (new BoxLayout(mainPanel, BoxLayout.Y_AXIS));	
		scrollPane.setViewportView(mainPanel); 
		
		deselectItem(); 
		setJMenuBar(new RPMenu(this, round.hasHappened())); 
		
		resetTopPanel(); 
		mainPanel.add(topPanel); 
		
		resetButtonsBar();
		mainPanel.add(buttonsBar); 
		
		//If it's an elim round, reset entries by placing them in the correct 
		//container (e.g. a noncompeting debate if they're not competing)
		if (round instanceof ElimRound 
			&& tournament.getBreaks().isEmpty() == false)
		{
			((ElimRound) round).resetBreakEntries(); 
		}
		
		//Make bar containing info for each flight 
		JPanel flightAPanel = makeFlightPanel('A'); 
		mainPanel.add(flightAPanel); 
		
		if (round.isFlighted())
		{
			JPanel flightBPanel = makeFlightPanel('B'); 
			mainPanel.add(flightBPanel);
		}
		
		resetBottomPanel(); 
		mainPanel.add(bottomPanel); 
		
		JTextField comments = new JTextField (round.getCommentsOnPostings()); 
		comments.getDocument().addDocumentListener (new CommentListener()); 
		comments.setMaximumSize(new Dimension(10000, 30));
		comments.setColumns(50); 
		mainPanel.add (comments); 
		
		validate(); 
		mainPanel.revalidate(); 
		mainPanel.repaint(); 

		//check so that we don't bring this panel to the front of the screen if
		//it's already open and just being refreshed from a different panel.
		if (isVisible() == false)
			setVisible(true); 
	}
	
	
	//---------------------------------------------------------------------------
	// Redraws top panel containing the name, day, and status of the round 
	//---------------------------------------------------------------------------
	private void resetTopPanel ()
	{
		JLabel name = new JLabel ("<html><strong>" 
								+ round.getName().toUpperCase() + "</strong></html>"); 
		
		JPanel day = new JPanel(); 
		day.setLayout (new BoxLayout(day, BoxLayout.X_AXIS)); 
		JLabel dayLabel = new JLabel ("Day:"); 
		JTextField dayField = new JTextField (round.getDay()); 
		dayField.getDocument().addDocumentListener (new DayListener()); 
		dayField.setColumns(6); 
		dayField.setMaximumSize(new Dimension(100,50)); 
		day.add(dayLabel); 
		day.add(dayField); 
		
		round.resetStatus(); 
		JLabel status = new JLabel (round.getStatusString()); 
		
		topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); 
		topPanel.add(name); 
		topPanel.add(day); 
		topPanel.add(status);
	}
	
	
	//--------------------------------------------------------------------------
	// Resets the bar towards the top containing several option buttons.
	//--------------------------------------------------------------------------
	private void resetButtonsBar ()
	{
		buttonsBar = new JPanel(); 
		buttonsBar.setLayout(new BoxLayout(buttonsBar, BoxLayout.X_AXIS)); 
		
		JPanel flightInfoPanel = new JPanel(); 
		flightInfoPanel.setLayout (new BoxLayout (flightInfoPanel, 
																				BoxLayout.X_AXIS)); 
		
		JCheckBox flighted = new JCheckBox ("Flighted"); 
		flighted.setSelected(round.isFlighted()); 
		flighted.addItemListener (new FlightListener()); 
		flighted.setHorizontalTextPosition (SwingConstants.LEFT); 
		flightInfoPanel.add(flighted); 
		
		buttonsBar.add(flightInfoPanel); 
		
		JButton lock = new JButton("Lock all"); 
		lock.addActionListener (new LockListener());
		lock.setMaximumSize(new Dimension(30,20)); 
		boolean shouldLock = lockChanges != null && lockChanges.isSelected();
		lockChanges = new JCheckBox ("Lock changes being made"); 
		lockChanges.setSelected(shouldLock);
		JButton unlock = new JButton ("Unlock all"); 
		unlock.addActionListener (new UnlockListener()); 
		unlock.setMaximumSize(new Dimension(30,20)); 
		JButton clear = new JButton ("Clear all"); 
		clear.addActionListener (new ClearListener()); 
		clear.setMaximumSize(new Dimension(30,20)); 
		
		JPanel buttons = new JPanel(); 
		buttons.setLayout (new GridLayout(2,2)); 
		buttons.add(lock); 
		buttons.add(lockChanges); 
		buttons.add(unlock); 
		buttons.add(clear); 
		
		buttonsBar.add(buttons); 
	}
	
	
	//--------------------------------------------------------------------------
	// Creates a panel showing all the information for a flight - start time, 
	// room, debaters, judge, room, and buttons to enter or view ballots. 
	//--------------------------------------------------------------------------
	private JPanel makeFlightPanel (char flight)
	{
		//Make the actual panel to be returned 
		JPanel flightPanel = new JPanel();
		flightPanel.setLayout (new GridBagLayout()); 
		
		//Add header at top 
		if (round.isFlighted())
		{
			JLabel flightLabel = new JLabel ("FLIGHT " + flight);
			gbc.gridx = 2;
			gbc.gridy = 0;
			gbc.gridwidth = 2; 
			flightPanel.add(flightLabel, gbc);
			gbc.gridwidth = 1;
		}
		
		//Then make panel showing time at the top 
		JPanel time = new JPanel(); 
		time.setLayout (new GridBagLayout()); 
		
		JLabel timeLabel = new JLabel ("Start time:"); 
		JTextField timeField = new JTextField(flight == 'A' ? round.getTime() : 
																		round.getTimeFlightB());
		timeField.getDocument().addDocumentListener (new TimeListener(flight)); 
		timeField.setColumns(6); 
		timeField.setMinimumSize(new Dimension(50, 20));
		
		JCheckBox asap = null; 
		if (flight == 'A')
		{
			asap = new JCheckBox ("ASAP"); 
			asap.setSelected(round.isASAP()); 
			asap.addItemListener (new ASAPListener()); 
			asap.setHorizontalTextPosition(SwingConstants.LEFT); 
		}
		
		gbc.gridx = 0;
		gbc.gridy = 1; 
		time.add(timeLabel, gbc); 
		
		gbc.gridx = 1; 
		gbc.gridy = 1; 
		time.add(timeField, gbc); 
		
		if (asap != null)
		{
			gbc.gridx = 0; 
			gbc.gridy = 2; 
			gbc.gridwidth = 2; 
			time.add(asap, gbc); 
			gbc.gridwidth = 1; 
		}
		
		//add time to panel 
		gbc.gridx = 0; 
		gbc.gridy = 1; 
		flightPanel.add(time, gbc); 
		
		//Create header row 
		JLabel room = new JLabel ("ROOM"); 
		
		gbc.gridx = 0; 
		gbc.gridy = 2;
		flightPanel.add(room, gbc); 
		
		if (round.isSidelocked() == true)
		{
			JLabel aff = new JLabel ("AFFIRMATIVE"); 
			JLabel neg = new JLabel ("NEGATIVE"); 
			
			gbc.gridx = 1; 
			gbc.gridy = 2; 
			flightPanel.add(aff, gbc); 
			
			gbc.gridx = 3; 
			gbc.gridy = 2; 
			flightPanel.add(neg, gbc); 
		}
		else
		{
			JLabel flip = new JLabel ("FLIP FOR SIDES"); 
			
			gbc.gridx = 1; 
			gbc.gridy = 2; 
			gbc.gridwidth = 2; 
			flightPanel.add(flip, gbc); 
			gbc.gridwidth = 1; 
		}
		
		JLabel judge = new JLabel ("JUDGE"); 
		
		gbc.gridx = 5; 
		gbc.gridy = 2; 
		flightPanel.add(judge, gbc); 
		
		//add blank lines to the list as necessary to fill space 
		if (round.hasHappened() == false && needToRefillBlanks == true)
			refillItemsOnPairings(); 
		
		int gridy = 3; //start right below the header row
		
		//If it's an elim round that hasn't started yet, but every round before it
		//has started, go through and remove every debater that isn't supposed to
		//be in the round and put them in a not competing pseudodebate. 
/*		if (round instanceof ElimRound && round.hasHappened() == false 
TODO		&& round.getPreviousRound() != null 
			&& round.getPreviousRound().hasHappened())
		{
			((ElimRound) round).removeNonBreakingTeams();
		}
*/		
		
		//Cycle through and add all items to pairings 
		for (Flightable obj : round.getItemsOnPairings())
		{
			//skip items from the wrong flight
			if (flight != obj.getFlight())
				continue;
			
			if (obj instanceof Debate)
				addDebateRow ((Debate) obj, flightPanel, gridy); 

			else if (obj instanceof JudgeAssignment)
				addJudgeRow ((JudgeAssignment) obj, flightPanel, gridy);

			else if (obj instanceof RoomAssignment)
				addRoomRow ((RoomAssignment) obj, flightPanel, gridy);
			
			else if (obj instanceof JudgeRoomAssignment)
				addJudgeRoomRow ((JudgeRoomAssignment) obj, flightPanel, gridy);

			else if (obj instanceof BlankLine) 
				addBlankRow ((BlankLine) obj, flightPanel, gridy);
			
			gridy++;	
		}

		flightPanel.setPreferredSize(new Dimension(this.getWidth(), 
									(int) flightPanel.getPreferredSize().getHeight()));
		return flightPanel; 
	}
	
	
	//--------------------------------------------------------------------------
	// Makes the bottom panel which shows debaters, judges, or rooms that are
	// not currently assigned to a debate. 
	//--------------------------------------------------------------------------
	private void resetBottomPanel ()
	{
		if (bottomPanel == null)
			bottomPanel = new JPanel(); 
		else	
			bottomPanel.removeAll(); 

		bottomPanel.setLayout (new BoxLayout(bottomPanel, BoxLayout.Y_AXIS)); 
		
		unassignedDebaters = new JRadioButton ("Unassigned Debaters"); 
		unassignedJudges = new JRadioButton ("Unassigned Judges"); 
		unassignedRooms = new JRadioButton ("Unassigned Rooms");
		
		ButtonGroup group = new ButtonGroup(); 
		group.add (unassignedDebaters); 
		group.add (unassignedJudges); 
		group.add (unassignedRooms);
		
		UnassignedListener listener = new UnassignedListener();
		unassignedDebaters.addActionListener(listener); 
		unassignedJudges.addActionListener(listener); 
		unassignedRooms.addActionListener(listener); 
	
		JPanel topBottomPanel = new JPanel(); 
		topBottomPanel.setLayout(new BoxLayout(topBottomPanel,BoxLayout.X_AXIS));
		topBottomPanel.add(unassignedDebaters);
		topBottomPanel.add(unassignedJudges);
		topBottomPanel.add(unassignedRooms);
		
		JPanel bottomBottomPanel = new JPanel(); 
		
		if (bottomPanelView == UNASSIGNED_DEBATERS)
		{
			unassignedDebaters.setSelected(true); 
			bottomBottomPanel = makeUnassignedDebatersPanel();
		}
		else if (bottomPanelView == UNASSIGNED_JUDGES)
		{
			unassignedJudges.setSelected(true); 
			bottomBottomPanel = makeUnassignedJudgesPanel();
		}
		else if (bottomPanelView == UNASSIGNED_ROOMS)
		{
			unassignedRooms.setSelected(true); 
			bottomBottomPanel = makeUnassignedRoomsPanel();
		}
		
		bottomPanel.add(topBottomPanel); 
		bottomPanel.add(bottomBottomPanel); 
	}
	
	
	//--------------------------------------------------------------------------
	// Makes the bottom panel when the debaters option is selected
	//--------------------------------------------------------------------------
	private JPanel makeUnassignedDebatersPanel ()
	{
		JPanel udp = new JPanel(); 
		udp.setLayout (new GridBagLayout()); 
		
		ArrayList<Debate> byes = round.getByes(); 
		ArrayList<Debate> ffts = round.getForfeits(); 
		ArrayList<Debate> ncs = round.getNotCompetings(); 
		ArrayList<Entry> unassigneds = round.getUnassignedDebaters(); 
		
		//go down left side, then down right side 
		int height = (int) Math.ceil((byes.size() + ffts.size() + ncs.size() 
									+ unassigneds.size()) / 2.0); 
		int gridy = 0; 
		int gridx = 0; 

		//Add all debaters scheduled to have byes 
		for (Debate bye : byes)
		{
			JPanel panel = new JPanel(); 
			panel.setLayout (new BoxLayout(panel, BoxLayout.X_AXIS)); 
			
			Entry entry = bye.getTeam1(); 
			panel.add(new SmartLabel(this, bye, entry)); 
			
			UnassignedDebaterOptionsPanel opts = new UnassignedDebaterOptionsPanel
																	(entry, Debate.Outcome.BYE);
			panel.add (opts); 
			
			if (gridy >= height)
			{
				gridx = 1; 
				gridy = 0; 
			}
			
			gbc.gridx = gridx; 
			gbc.gridy = gridy; 
			udp.add (panel, gbc); 
			
			gridy++; 
		}

		//Add all debaters scheduled to have forfeits 
		for (Debate fft : ffts)
		{
			JPanel panel = new JPanel(); 
			panel.setLayout (new BoxLayout(panel, BoxLayout.X_AXIS)); 
			
			Entry entry = fft.getTeam1(); 
			panel.add(new SmartLabel(this, fft, entry)); 
			
			UnassignedDebaterOptionsPanel opts = new UnassignedDebaterOptionsPanel
															(entry, Debate.Outcome.FORFEIT);
			panel.add (opts); 
			
			if (gridy >= height)
			{
				gridx = 1; 
				gridy = 0; 
			}
			
			gbc.gridx = gridx; 
			gbc.gridy = gridy; 
			udp.add (panel, gbc); 
			
			gridy++; 
		}

		//Add all debaters who are scheduled to not compete in this round  
		for (Debate nc : ncs)
		{
			//If it's an elim, skip this b/c not competings are not visible. 
			if (round instanceof ElimRound)
				break;
		
			JPanel panel = new JPanel(); 
			panel.setLayout (new BoxLayout(panel, BoxLayout.X_AXIS)); 
			
			Entry entry = nc.getTeam1(); 
			panel.add(new SmartLabel(this, nc, entry)); 
			
			UnassignedDebaterOptionsPanel opts = new UnassignedDebaterOptionsPanel
													(entry, Debate.Outcome.NOT_COMPETING);
			panel.add (opts); 
			
			if (gridy >= height)
			{
				gridx = 1; 
				gridy = 0; 
			}
			
			gbc.gridx = gridx; 
			gbc.gridy = gridy; 
			udp.add (panel, gbc); 
			
			gridy++; 
		}

		//Add all debaters who just haven't been assigned to a debate yet   
		for (Entry entry : unassigneds)
		{
			JPanel panel = new JPanel(); 
			panel.setLayout (new BoxLayout(panel, BoxLayout.X_AXIS)); 
			
			panel.add(new SmartLabel(this, null, entry)); 
			
			UnassignedDebaterOptionsPanel opts = new UnassignedDebaterOptionsPanel
																					(entry, null);
			panel.add (opts); 
			
			if (gridy >= height)
			{
				gridx = 1; 
				gridy = 0; 
			}
			
			gbc.gridx = gridx; 
			gbc.gridy = gridy; 
			udp.add (panel, gbc); 
			
			gridy++; 
		}
		
		return udp; 
	}


	//--------------------------------------------------------------------------
	// Makes the bottom panel when the judges option is selected
	//--------------------------------------------------------------------------
	private JPanel makeUnassignedJudgesPanel ()
	{
		JPanel ujp = new JPanel(); 
		ujp.setLayout (new GridBagLayout()); 
		
		//go down left side, then down right side 
		int height = (int) Math.ceil(tournament.getJudges().size() / 2.0); 
		int gridy = 0; 
		int gridx = 0; 
		
		for (Judge judge : tournament.getUnassignedJudges(round))
		{
			JPanel panel = new JPanel(); 
			panel.setLayout (new BoxLayout(panel, BoxLayout.X_AXIS)); 
			SmartLabel label = new SmartLabel(this, null, judge); 
			panel.add(label); 
			panel.add(new PriorityBox(judge)); 			

			if (gridy >= height)
			{
				gridx = 1; 
				gridy = 0; 
			}
			
			gbc.gridx = gridx; 
			gbc.gridy = gridy; 
			ujp.add (panel, gbc); 
			
			gridy++; 
		}
		
		return ujp; 
	}



	//--------------------------------------------------------------------------
	// Makes the bottom panel when the rooms option is selected
	//--------------------------------------------------------------------------
	private JPanel makeUnassignedRoomsPanel ()
	{
		JPanel urp = new JPanel(); 
		urp.setLayout (new GridBagLayout()); 
		
		//go down left side, then down right side 
		int height = (int) Math.ceil(tournament.getUnassignedRooms(round).size() 
							/ 2.0); 
		int gridy = 0; 
		int gridx = 0; 
		
		for (Room room : tournament.getUnassignedRooms(round))
		{
			JPanel panel = new JPanel(); 
			panel.setLayout (new BoxLayout(panel, BoxLayout.X_AXIS)); 
			SmartLabel label = new SmartLabel(this, null, room); 
			panel.add(label); 
			panel.add(new PriorityBox(room)); 			
			
			if (gridy >= height)
			{
				gridx = 1; 
				gridy = 0; 
			}
			
			gbc.gridx = gridx; 
			gbc.gridy = gridy; 
			urp.add (panel, gbc); 
			
			gridy++; 
		}
		
		return urp; 
	}
	

	//--------------------------------------------------------------------------
	// Adds blank lines as necessary to the itemsOnPairings array lists so that 
	// there are enough spaces in order to add every entry to a debate.  
	//--------------------------------------------------------------------------
	private void refillItemsOnPairings() 
	{
		ArrayList<Flightable> itemsOnPairings = round.getItemsOnPairings(); 
	
		//Get the number of teams currently assigned to "true" debates. This is
		//true because the total number of teams must equal those assigned to 
		//true debates + pseudo debates + unassigned. 
		int numTeamsTrueDebates = tournament.getEntries().size() - 
		  round.getPseudoDebates().size() - round.getUnassignedDebaters().size();

		//Get the number of available spaces in "true" debates 
		int numSpacesAvailable= 2*(itemsOnPairings.size()) - numTeamsTrueDebates;
	
		//number of additional debates that will need to be created in order for 
		//there to be enough room for every entry that is not currently assigned 
		//to a debate (whether a true or pseudo debate). 
		int numBlanksNeeded = (round.getUnassignedDebaters().size() 
									- numSpacesAvailable) / 2; //integer division
		
		//Add blank lines, to balance out between flights if necessary 
		int n = 0; 
		int numItemsA = getNumItemsInFlight ('A');
		int numItemsB = getNumItemsInFlight ('B');
		
		while (n < numBlanksNeeded)
		{
			if (round.isFlighted() == false || numItemsA <= numItemsB)
			{
				itemsOnPairings.add (new BlankLine('A'));
				numItemsA++;
			}
			else
			{
				itemsOnPairings.add (new BlankLine('B'));
				numItemsB++;
			}
			
			n++;
		}
		
		//reset boolean, blanks have now been reset. 
		needToRefillBlanks = false; 
	}
	


	//--------------------------------------------------------------------------
	// Adds a single blank line to the list. 
	//--------------------------------------------------------------------------
	void addBlankLine ()
	{
		if (round.isFlighted() == false 
			|| getNumItemsInFlight('A') <= getNumItemsInFlight('B'))
		{
			round.getItemsOnPairings().add (new BlankLine('A')); 
			initialize(tf);
		}
		else
		{
			round.getItemsOnPairings().add (new BlankLine('B'));
			initialize(tf);
		}
	}


	//--------------------------------------------------------------------------
	// Deletes a single blank line from the list. If the round is flighted, it
	// first tries to remove from the flight with more items. 
	//--------------------------------------------------------------------------
	void deleteBlankLine ()
	{
		if (round.isFlighted() == false)
		{
			deleteBlankLine('A');
		}
		
		else 
		{
			//flight with more and fewer items, respectively 
			char largerFlight = (getNumItemsInFlight('A') 
					> getNumItemsInFlight('B')) ? 'A' : 'B';
			char smallerFlight = (largerFlight == 'A') ? 'B' : 'A';
			
			boolean success = deleteBlankLine (largerFlight); 
			
			if (!(success))
				deleteBlankLine(smallerFlight); 
		}
	}


	//--------------------------------------------------------------------------
	// Deletes a blank line from a specific flight. Returns a boolean indicating 
	// whether or not an item was removed (it will return false iff there were 
	// no blanklines in that flight.
	//--------------------------------------------------------------------------
	boolean deleteBlankLine (char flight)
	{
		ArrayList<Flightable> itemsOnPairings = round.getItemsOnPairings(); 
	
		if (itemsOnPairings.isEmpty())
			return false; 
	
		int i = itemsOnPairings.size() - 1;
		
		while (i >= 0)
		{
			if (itemsOnPairings.get(i) instanceof BlankLine 
				&& itemsOnPairings.get(i).getFlight() == flight)
			{
				itemsOnPairings.remove(i);
				initialize(tf); 
				return true;
			}
			
			i--; 
		}
		
		return false; 
	}


	//--------------------------------------------------------------------------
	// Adds a row containing a debate (which may be incomplete, but contains at
	// least one debater) to the flight panel. 
	//--------------------------------------------------------------------------
	private void addDebateRow (Debate debate, JPanel flightPanel, int gridy)
	{
		//Add room 
		gbc.gridx = 0; 
		gbc.gridy = gridy; 
		if (debate.getRoom() == null)
			flightPanel.add (new SmartLabel(this, debate, 
									SmartLabel.BlankType.ROOM), gbc);
		else 
			flightPanel.add (new SmartLabel(this, debate, debate.getRoom()), gbc);
		
		//Add lefthand debater 
		gbc.gridx = 1; 
		if (debate.getTeam1() == null)
			flightPanel.add (new SmartLabel(this, debate, 
									SmartLabel.BlankType.LEFT_DEBATER), gbc); 
		else 
		{
			SmartLabel teamLabel = new SmartLabel(this, debate, debate.getTeam1());
			flightPanel.add(teamLabel, gbc);
			
			if (debate.isSpecialSidelocked())
				teamLabel.setText(teamLabel.getText() + " (AFF)");
		}
				
		
		//Add decision for lefthand debater 
		JLabel decision1 = new JLabel(debate.getDecision(0).toShortString());
		if (debate.isBallotEntered())
			decision1.setForeground(Color.BLACK);
		else
			decision1.setForeground(Color.BLUE);
		gbc.gridx = 2; 
		flightPanel.add(decision1, gbc);

		//Add righthand debater
		gbc.gridx = 3; 
		if (debate.getTeam2() == null)
			flightPanel.add (new SmartLabel(this, debate, 
									SmartLabel.BlankType.RIGHT_DEBATER), gbc); 
		else 
			flightPanel.add(new SmartLabel(this, debate, debate.getTeam2()), gbc);


		//Add decision for righthand debater 
		JLabel decision2 = new JLabel(debate.getDecision(1).toShortString());
		if (debate.isBallotEntered())
			decision2.setForeground(Color.BLACK);
		else
			decision2.setForeground(Color.BLUE);
		gbc.gridx = 4; 
		flightPanel.add(decision2, gbc);
		
		//Add judge(s)  
		gbc.gridx = 5; 
		flightPanel.add (makeJudgeLabels(debate), gbc);
		
		//Add button to enter or view ballot 
		if (round.hasHappened() && (round instanceof PracticeRound == false))
		{
			JButton ballotButton = new JButton (); 

			if (debate.isBallotEntered())
				ballotButton.setText("View ballot"); 
			else
				ballotButton.setText("Enter ballot"); 
			
			ballotButton.addActionListener (new ViewBallotListener(debate)); 
			if (debate.getNumTeams() == 2 
				&& debate.getJudges().size() == round.getNumJudges() 
				&& debate.getRoom() != null)
			{
				ballotButton.setEnabled(true);
			}
			else
				ballotButton.setEnabled(false); 

			gbc.gridx = 6; 
			flightPanel.add (ballotButton, gbc); 		
		}
	}  
	
	
	
	//--------------------------------------------------------------------------
	// Adds a row to the flight panel with a judge but no room or debaters 
	//--------------------------------------------------------------------------
	private void addJudgeRow (JudgeAssignment judgeass, JPanel flightPanel, 
									int gridy)
	{
		gbc.gridx = 0; 
		gbc.gridy = gridy; 
		flightPanel.add (new SmartLabel(this, judgeass, 
								SmartLabel.BlankType.ROOM), gbc);

		gbc.gridx = 1; 
		flightPanel.add (new SmartLabel(this, judgeass, 
							SmartLabel.BlankType.LEFT_DEBATER), gbc);
		
		
		//Add (non smart label) dashes in the spaces for aff and neg's decisions
		JLabel decision1 = new JLabel("-");
		decision1.setForeground(Color.BLUE); //no blanks in rounds that's started
		gbc.gridx = 2; 
		flightPanel.add(decision1, gbc);

		gbc.gridx = 3; 
		flightPanel.add (new SmartLabel(this, judgeass, 
							SmartLabel.BlankType.RIGHT_DEBATER), gbc);
		
		JLabel decision2 = new JLabel("-");
		decision2.setForeground(Color.BLUE); //no blanks in round that's started
		gbc.gridx = 4; 
		flightPanel.add(decision2, gbc);
		
		//add judge(s) 
		gbc.gridx = 5; 
		flightPanel.add (makeJudgeLabels(judgeass), gbc);
	}  


	//--------------------------------------------------------------------------
	// Adds a row to the flight panel with a room but no judge or debaters 
	//--------------------------------------------------------------------------
	private void addRoomRow (RoomAssignment roomass, JPanel flightPanel, 
									int gridy) 
	{
		gbc.gridx = 0; 
		gbc.gridy = gridy; 
		flightPanel.add (new SmartLabel(this, roomass, roomass.getRoom()), gbc);

		gbc.gridx = 1; 
		flightPanel.add (new SmartLabel(this, roomass, 
							SmartLabel.BlankType.LEFT_DEBATER), gbc);
		
		//Add (non smart label) dashes in the spaces for aff and neg's decisions
		JLabel decision1 = new JLabel("-");
		decision1.setForeground(Color.BLUE); //no blanks in rounds that's started
		gbc.gridx = 2; 
		flightPanel.add(decision1, gbc);

		gbc.gridx = 3; 
		flightPanel.add (new SmartLabel(this, roomass, 
							SmartLabel.BlankType.RIGHT_DEBATER), gbc);
		
		JLabel decision2 = new JLabel("-");
		decision2.setForeground(Color.BLUE); //no blanks in round that's started
		gbc.gridx = 4; 
		flightPanel.add(decision2, gbc);
		
		//add blank space for judge
		gbc.gridx = 5; 
		flightPanel.add (makeJudgeLabels(roomass), gbc);
	}  

	//--------------------------------------------------------------------------
	// Adds a row to the flight panel with a judge and room but no debaters 
	//--------------------------------------------------------------------------
	private void addJudgeRoomRow (JudgeRoomAssignment jrass, JPanel flightPanel, 
									int gridy) 
	{
		gbc.gridx = 0; 
		gbc.gridy = gridy; 
		flightPanel.add (new SmartLabel(this, jrass, jrass.getRoom()), gbc);

		gbc.gridx = 1; 
		flightPanel.add (new SmartLabel(this, jrass, 
							SmartLabel.BlankType.LEFT_DEBATER), gbc);
		
		//Add (non smart label) dashes in the spaces for aff and neg's decisions
		JLabel decision1 = new JLabel("-");
		decision1.setForeground(Color.BLUE); //no blanks in rounds that's started
		gbc.gridx = 2; 
		flightPanel.add(decision1, gbc);

		gbc.gridx = 3; 
		flightPanel.add (new SmartLabel(this, jrass, 
							SmartLabel.BlankType.RIGHT_DEBATER), gbc);
		
		JLabel decision2 = new JLabel("-");
		decision2.setForeground(Color.BLUE); //no blanks in round that's started
		gbc.gridx = 4; 
		flightPanel.add(decision2, gbc);
		
		//add judge(s)
		gbc.gridx = 5; 
		flightPanel.add (makeJudgeLabels(jrass), gbc);
	}  




	//--------------------------------------------------------------------------
	// Adds a blank row to the flight panel, where debates may be added. Adds it
	// to "flightPanel" at row "gridy". 
	//--------------------------------------------------------------------------
	private void addBlankRow (BlankLine blank, JPanel flightPanel, int gridy)
	{
		gbc.gridx = 0; 
		gbc.gridy = gridy; 
		flightPanel.add (new SmartLabel(this, blank, SmartLabel.BlankType.ROOM), 
								gbc);

		gbc.gridx = 1; 
		flightPanel.add (new SmartLabel(this, blank, 
							SmartLabel.BlankType.LEFT_DEBATER), gbc);
		
		//Add (non smart label) dashes in the spaces for aff and neg's decisions
		JLabel decision1 = new JLabel("-");
		decision1.setForeground(Color.BLUE); //no blanks in round that's started
		gbc.gridx = 2; 
		flightPanel.add(decision1, gbc);

		gbc.gridx = 3; 
		flightPanel.add (new SmartLabel(this, blank, 
							SmartLabel.BlankType.RIGHT_DEBATER), gbc);
		
		JLabel decision2 = new JLabel("-");
		decision2.setForeground(Color.BLUE); //no blanks in round that's started
		gbc.gridx = 4; 
		flightPanel.add(decision2, gbc);

		//add blank space for judge 
		gbc.gridx = 5; 
		flightPanel.add (makeJudgeLabels(blank), gbc);
	}  




	//---------------------------------------------------------------------------
	// Returns a either a SmartLabel for a judge or a blank spot where a judge 
	// can go, or (if it's a round with multiple judges) a panel containing 
	// however many labels are necessary. 
	//---------------------------------------------------------------------------
	private JComponent makeJudgeLabels (Flightable container)
	{
		//First, if it's a debate or judge[room]assignment 
		if (container instanceof JudgeInhabitable 
			&& ((JudgeInhabitable) container).getJudge() != null)
		{
			if (round.getNumJudges() == 1)
			{
				return new SmartLabel (this, container, 
												((JudgeInhabitable) container).getJudge());
			}
			else
			{
				JPanel panel = new JPanel(); 
				panel.setLayout (new BoxLayout (panel, BoxLayout.Y_AXIS)); 
				
				int i = 0; 
				
				while (i < ((JudgeInhabitable) container).getJudges().size())
				{
					panel.add (new SmartLabel(this, container, 
									((JudgeInhabitable) container).getJudges().get(i)));
					i++;
				}
				
				//fill spaces, if there aren't the full number of judges 
				while (i < round.getNumJudges())
				{
					panel.add (new SmartLabel(this, container, 
									SmartLabel.BlankType.JUDGE));
					i++;
				}
				
				return panel; 
			}
		}

		//Next, if it's a RoomAssignment or BlankLine 
		else
		{
			if (round.getNumJudges() == 1)
			{
				return new SmartLabel (this, container, SmartLabel.BlankType.JUDGE);
			}
			else
			{
				JPanel panel = new JPanel(); 
				panel.setLayout (new BoxLayout (panel, BoxLayout.Y_AXIS)); 
			
				int i = 0; 

				while (i < round.getNumJudges())
				{
					panel.add (new SmartLabel(this, container, 
									SmartLabel.BlankType.JUDGE));
					i++;
				}
				
				return panel; 
			}
		}
	}

	
	//Accessors
	Round getRound()
	{
		return round;
	}

	SmartLabel getSelectedItem ()
	{
		return selectedItem;
	}
	
	//--------------------------------------------------------------------------
	// Called by a Mouse Listener of a SmartPanel, this sets the currently 
	// selected item. 
	//--------------------------------------------------------------------------
	void selectItem (SmartLabel item)
	{
		selectedItem = item; 
	}


	//---------------------------------------------------------------------------
	// Deselects the currently selected item, if any.
	//---------------------------------------------------------------------------
	void deselectItem ()
	{
		if (selectedItem == null)
			return; 
		
		selectedItem.deselect(); 
		selectedItem = null; 
	}



	//--------------------------------------------------------------------------
	// Returns the number of items in the itemsOnPairings array in the given 
	// flight. 
	//--------------------------------------------------------------------------
	int getNumItemsInFlight (char flight)
	{
		int n = 0; 
		
		for (Flightable obj : round.getItemsOnPairings())
			if (obj.getFlight() == flight)
				n++;
	
		return n;
	}
	

	//--------------------------------------------------------------------------
	// Sets whether blank spaces will be added for debates that still need to be
	// assigned the next time this panel is opened. 
	//--------------------------------------------------------------------------
	void setNeedToRefillBlanks (boolean need)
	{
		needToRefillBlanks = need; 
	}


	//--------------------------------------------------------------------------
	// This is the method that does the work of switching any objects to a 
	// different spot on the pairings - e.g. switching two debaters, adding a
	// classroom to a debate, etc. 
	/*
		Items that can be swapped: 
		-Entry and Entry 
			-Two entries in true debate 
			-Entry in null container with entry in true debate
			-Entry in pseudo debate with entry in true debate 
		-Entry and null  
			-Entry in true debate, in  null container, or pseudo debate 
			-null in BlankLine, Debate, judgeass, roomass, jrass 
		-Judge and Judge
			-any combination of judges in debate(s), judgeassignment(s), and 
				judgeroomassignment(s)
			-judge in null container with a judge in a debate/judgeass/jrass 
		-Judge and null  
			-judge can be in debate, judgeass, jrass, or null container
			-null may be in blankline, debate, or roomass
		-Room: same as Judge
		
		Note that null container or pseudo debate means it's on the bottom panel.
		Don't confuse null container (bottom panel, unassigned) with null object
		(one in BlankLine). 
		
	*/
	// obj1 and obj2 are the objects to be swapped - entries, judges, rooms, or
	// null - and container1 and container2 are their respective containers. 	
	//
	// The optional fourth argument debaterSide is ONLY necessary if you're 
	// swapping a debater into a BlankLine or Assignment and need to indicate
	// whether it's a LEFT_DEBATER or RIGHT_DEBATER spot. 
	//--------------------------------------------------------------------------
	void swap (Object obj1, Flightable container1, Object obj2, 
					Flightable container2)
	{
		//This default version of the method should be used any time the last 
		//argument is irrelevant. There is zero significance to the argument
		//LEFT_DEBATER - it will be unused. 
		swap (obj1, container1, obj2, container2, 
			SmartLabel.BlankType.LEFT_DEBATER);
	}
	
	void swap (Object obj1, Flightable container1, Object obj2, 
				Flightable container2, SmartLabel.BlankType debaterSide)
	{
		if (round instanceof ElimRound 
			&& (obj1 instanceof Entry || obj2 instanceof Entry))
		{
			int choice = JOptionPane.showConfirmDialog(this, "Manually pairing "
				+ "outrounds means that pairings\nin subsequent outrounds may be "
				+ "incorrect - are you sure you want\nto make this change?", 
				"Pair Outround", JOptionPane.YES_NO_OPTION, 
				JOptionPane.WARNING_MESSAGE);
			if (choice != JOptionPane.YES_OPTION)
				return; 
		}
	
		if (obj1 instanceof Entry && obj2 instanceof Entry)
		{
			Entry entry1 = (Entry) obj1;
			Entry entry2 = (Entry) obj2;
		
			//If both are in a debate
			if (container1 instanceof Debate && container2 instanceof Debate)
			{
				Debate debate1 = (Debate) container1; 
				Debate debate2 = (Debate) container2;
				
				//If both are in a true debate, swap them 
				if (debate1.isTrueDebate() && debate2.isTrueDebate())
				{
					int index1 = entry1.equals(debate1.getTeam1()) ? 0 : 1; 
					int index2 = entry2.equals(debate2.getTeam1()) ? 0 : 1; 
					
					debate1.setTeam(entry2, index1);
					debate2.setTeam(entry1, index2);
					debate1.setTeamLocked(entry2, lockChanges.isSelected());
					debate2.setTeamLocked(entry1, lockChanges.isSelected()); 
				}
				
				//If one is in a true debate and the other is in a pseudo debate, 
				//add the team from the pseudo debate into the real debate, and 
				//delete the pseudo debate.
				else
				{
					int index; //index in the true debate
					
					if (debate1.isTrueDebate())
					{
						index = entry1.equals(debate1.getTeam1()) ? 0 : 1;
						debate1.setTeam(entry2, index);
						debate1.setTeamLocked(entry2, lockChanges.isSelected()); 
						round.removeDebate (debate2);
					}
					else //if debate2 is a true debate
					{
						index = entry2.equals(debate2.getTeam1()) ? 0 : 1;
						debate2.setTeam(entry1, index);
						debate2.setTeamLocked(entry1, lockChanges.isSelected());
						round.removeDebate (debate1);
					}					
				}
			}
			
			//If one is in a real debate, and the other is in a null container, 
			//add the one who was in null into the debate. 
			else
			{
				int index; //index in the true debate

				if (container1 instanceof Debate)
				{
					Debate debate1 = (Debate) container1; 
					index = entry1.equals(debate1.getTeam1()) ? 0 : 1;
					debate1.setTeam(entry2, index);
					debate1.setTeamLocked (entry2, lockChanges.isSelected()); 
				}
				else //if debate2 is a true debate
				{
					Debate debate2 = (Debate) container2; 
					index = entry2.equals(debate2.getTeam1()) ? 0 : 1;
					debate2.setTeam(entry1, index);
					debate2.setTeamLocked(entry1, lockChanges.isSelected()); 
				}					
			}
		}//end of if both are Entries
		
		else if (obj1 instanceof Entry && obj2 == null) 
		{
			//if they're both in the same debate, switch the debater's "number".
			if (container1 instanceof Debate && container2 instanceof Debate 
				&& ((Debate) container1).equals((Debate) container2))
			{
				int newIndex = ((Entry) obj1).equals(((Debate) container1).getTeam1(
																							)) ? 1 : 0;
				((Debate) container1).removeTeam((Entry) obj1); 
				((Debate) container1).setTeam((Entry) obj1, newIndex);				
			}
			else{
		
			//if the entry is in a debate, remove it from the debate.
			if (container1 instanceof Debate)
			{
				Debate debate = (Debate) container1;
				debate.removeTeam((Entry) obj1);
				
				//if they were the only debater in that debate, then it's no longer
				//a debate - convert it to an assignment or blankline. 
				if (debate.getNumTeams() == 0)
					convertDebate (debate);
			}
			
			//THEN, decide where to put the debater: 
			
			//If the other container is a debate, put the entry in that debate.
			if (container2 instanceof Debate)
			{
				Debate debate = (Debate) container2; 
				
				if (debate.getTeam1() == null)
					debate.setTeam((Entry) obj1, 0);
				if (debate.getTeam2() == null)
					debate.setTeam((Entry) obj1, 1);
				
				debate.setTeamLocked ((Entry) obj1, lockChanges.isSelected()); 
			}
			
			//Otherwise, the other container will be a BlankLine or Assignment. 
			//Convert it into a debate, then add the new debater to it. 
			else 
			{
				Debate debate = convertObjectToDebate (container2);
				debate.setTeam((Entry) obj1, 
					debaterSide.equals(SmartLabel.BlankType.LEFT_DEBATER) ? 0 : 1);
				debate.setTeamLocked ((Entry) obj1, lockChanges.isSelected()); 
			}
			}//end of else 
		}
		
		//This is copy-pasted from the block above (with obj1 and obj2 reversed)
		else if (obj2 instanceof Entry && obj1 == null)
		{
			//if they're both in the same debate, switch the debater's "number".
			if (container2 instanceof Debate && container1 instanceof Debate 
				&& ((Debate) container2).equals((Debate) container1))
			{
				int newIndex = ((Entry) obj2).equals(((Debate) container2).getTeam1(
																							)) ? 1 : 0;
				((Debate) container2).removeTeam((Entry) obj2); 
				((Debate) container2).setTeam((Entry) obj2, newIndex);
			}
			else{
			
			//if the entry is in a debate, remove it from the debate.
			if (container2 instanceof Debate)
			{
				Debate debate = (Debate) container2;
			
				debate.removeTeam((Entry) obj2);
				
				//if they were the only debater in that debate, then it's no longer
				//a debate - convert it to an assignment or blankline. 
				if (debate.getNumTeams() == 0)
					convertDebate (debate);
			}
			
			//THEN, decide where to put the debater: 
			
			//If the other container is a debate, put the entry in that debate.
			if (container1 instanceof Debate)
			{
				Debate debate = (Debate) container1; 
				
				if (debate.getTeam1() == null)
					debate.setTeam((Entry) obj2, 0);
				if (debate.getTeam2() == null)
					debate.setTeam((Entry) obj2, 1);

				debate.setTeamLocked ((Entry) obj2, lockChanges.isSelected()); 
			}
			
			//Otherwise, the other container will be a BlankLine or Assignment. 
			//Convert it into a debate, then add the new debater to it. 
			else 
			{
				Debate debate = convertObjectToDebate (container1);
				debate.setTeam((Entry) obj2, 
					debaterSide.equals(SmartLabel.BlankType.LEFT_DEBATER) ? 0 : 1);
				debate.setTeamLocked((Entry) obj2, lockChanges.isSelected()); 
			}
			}//end of else
		}

		
		else if (obj1 instanceof Judge && obj2 instanceof Judge)
		{
			//If one of the judges is in a null container, remove the other judge
			//from their container and add the previously null one in its place.
			if (container1 == null)
			{
				JudgeInhabitable container = (JudgeInhabitable) container2; 
				Judge judge1 = (Judge) obj1; //judge being added to container
				Judge judge2 = (Judge) obj2; //judge being removed from container
				container.removeJudge(judge2);
				container.addJudge(judge1); 
				
				judge2.setLocked(container, false);
				judge1.setLocked(container, lockChanges.isSelected()); 
			}
			else if (container2 == null)
			{
				JudgeInhabitable container = (JudgeInhabitable) container1; 
				Judge judge1 = (Judge) obj1; //judge being added to container
				Judge judge2 = (Judge) obj2; //judge being removed from container
				container.removeJudge(judge1); 
				container.addJudge(judge2); 
				
				judge1.setLocked(container, false);
				judge2.setLocked(container, lockChanges.isSelected()); 
			}
			//Otherwise, switch the judges in their containers
			else
			{
				Judge judge1 = (Judge) obj1; 
				Judge judge2 = (Judge) obj2; 
				JudgeInhabitable cont1 = (JudgeInhabitable) container1; 
				JudgeInhabitable cont2 = (JudgeInhabitable) container2;
				
				cont1.removeJudge(judge1); 
				judge1.setLocked(cont1, false); 
				cont1.addJudge(judge2); 
				judge2.setLocked(cont1, lockChanges.isSelected()); 
				
				cont2.removeJudge(judge2); 
				judge2.setLocked(cont2, false);
				cont2.addJudge(judge1);
				judge1.setLocked(cont1, lockChanges.isSelected()); 
			}
		}

		else if (obj1 instanceof Judge && obj2 == null) 
		{
			Judge judge = (Judge) obj1; 
			
			//FIRST, remove the judge from its current container
			if (container1 instanceof Debate)
				((Debate) container1).removeJudge(judge);
			
			//If not a debate, the judge may be in a judgeassignment or 
			//judgeroomassignment. Otherwise, the judge is in a null container,
			//so we don't need to remove them from anything. 
			else if (container1 instanceof JudgeInhabitable)
			{
				//If it only has one judge, remove it and change it to a roomassmnt
				//or blankline. 
				if (((JudgeInhabitable) container1).getJudges().size() == 1)
					convertToObjWithoutJudge ((JudgeInhabitable) container1);
				else //otherwise, just remove that judge. 
					((JudgeInhabitable) container1).removeJudge(judge);
			}
			
			//SECOND, add the judge to the new container

			//This is generally for if it's a debate.  It could also possible be a
			//Judge[Room]Assignment - this will only be the case if it's an 
			//outround with multiple judges, because otherwise this wouldn't 
			//contain a null judge. 
			if (container2 instanceof JudgeInhabitable) 
			{
				((JudgeInhabitable) container2).addJudge(judge); 
				judge.setLocked ((JudgeInhabitable) container2,		
									lockChanges.isSelected()); 
			}
			
			//Otherwise, container2 will be a roomassignment or blankline
			else
			{
				JudgeInhabitable cont2 = convertToJudgeInhabitable (container2);
				cont2.addJudge(judge); //cont2 doesn't have any judges yet 
				judge.setLocked(cont2, lockChanges.isSelected());
			}
		}
		//Copy and pasted from above, with obj1 and obj2 switched 
		else if (obj2 instanceof Judge && obj1 == null)
		{
			Judge judge = (Judge) obj2; 
		
			//FIRST, remove the judge from its current container
			if (container2 instanceof Debate)
				((Debate) container2).removeJudge(judge);
			
			//If not a debate, the judge may be in a judgeassignment or 
			//judgeroomassignment. Otherwise, the judge is in a null container,
			//so we don't need to remove them from anything. 
			else if (container2 instanceof JudgeInhabitable)
			{	
				//If it only has one judge, remove it and change it to a roomassmnt
				//or blankline. 
				if (((JudgeInhabitable) container2).getJudges().size() == 1)
					convertToObjWithoutJudge ((JudgeInhabitable) container2);
				else //otherwise, just remove that judge. 
					((JudgeInhabitable) container2).removeJudge(judge);
			}
			
			//SECOND, add the judge to the new container

			//This is generally for if it's a debate.  It could also possible be a
			//Judge[Room]Assignment - this will only be the case if it's an 
			//outround with multiple judges, because otherwise this wouldn't 
			//contain a null judge. 
			if (container1 instanceof JudgeInhabitable) 
			{
				((JudgeInhabitable) container1).addJudge(judge); 
				judge.setLocked ((JudgeInhabitable) container1,		
									lockChanges.isSelected()); 
			}
			
			//Otherwise, container1 will be a roomassignment or blankline
			else
			{
				JudgeInhabitable cont1 = convertToJudgeInhabitable (container1);
				cont1.setJudge(judge); //cont1 doesn't have any judges yet 
				judge.setLocked(cont1, lockChanges.isSelected());
			}
		}
		//end of Judge methods


		else if (obj1 instanceof Room && obj2 instanceof Room)
		{
			//If one of the rooms is in a null container, remove the other room
			//from their container and add the previously null one in its place.
			if (container1 == null)
			{
				RoomInhabitable container = (RoomInhabitable) container2; 
				container.setRoom((Room) obj1);
				container.setRoomLocked (lockChanges.isSelected()); 
			}
			else if (container2 == null)
			{
				RoomInhabitable container = (RoomInhabitable) container1; 
				container.setRoom((Room) obj2);
				container.setRoomLocked (lockChanges.isSelected()); 
			}
			//Otherwise, switch the rooms in their containers
			else
			{
				Room room1 = (Room) obj1; 
				Room room2 = (Room) obj2; 
				RoomInhabitable cont1 = (RoomInhabitable) container1; 
				RoomInhabitable cont2 = (RoomInhabitable) container2;
				
				cont1.setRoom (room2);
				cont2.setRoom (room1);  
				cont1.setRoomLocked (lockChanges.isSelected()); 
				cont2.setRoomLocked (lockChanges.isSelected()); 
			}
		}

		else if (obj1 instanceof Room && obj2 == null) 
		{
			Room room = (Room) obj1; 
		
			//FIRST, remove the room from its current container
			if (container1 instanceof Debate)
			{
				((Debate) container1).setRoom(null);
				((Debate) container1).setRoomLocked(lockChanges.isSelected());
			}
			
			//If not, the room may be in a roomassignment or 
			//judgeroomassignment. Otherwise, the room is in a null container,
			//so we don't need to remove it from anything. 
			else if (container1 instanceof RoomInhabitable)
				convertToObjWithoutRoom ((RoomInhabitable) container1);
			
			//SECOND, add the room to the new container
			if (container2 instanceof Debate)
			{
				((Debate) container2).setRoom(room);
				((Debate) container2).setRoomLocked(lockChanges.isSelected()); 
			}
			
			//Otherwise, container2 will be a roomassignment or blankline
			else
			{
				RoomInhabitable cont2 = convertToRoomInhabitable (container2);
				cont2.setRoom(room);
				cont2.setRoomLocked(lockChanges.isSelected()); 
			}
		}
		//Copy and pasted from above, with obj1 and obj2 switched 
		else if (obj2 instanceof Room && obj1 == null)
		{
			Room room = (Room) obj2; 
		
			//FIRST, remove the room from its current container
			if (container2 instanceof Debate)
			{
				((Debate) container2).setRoom(null);
				((Debate) container2).setRoomLocked(lockChanges.isSelected()); 
			}
			
			//If not, the room may be in a roomassignment or 
			//judgeroomassignment. Otherwise, the room is in a null container,
			//so we don't need to remove it from anything. 
			else if (container2 instanceof RoomInhabitable)
				convertToObjWithoutRoom ((RoomInhabitable) container2);
			
			//SECOND, add the room to the new container
			if (container1 instanceof Debate)
			{
				((Debate) container1).setRoom(room);
				((Debate) container1).setRoomLocked(lockChanges.isSelected());
			}
			
			//Otherwise, container1 will be a roomassignment or blankline
			else
			{
				RoomInhabitable cont1 = convertToRoomInhabitable (container1);
				cont1.setRoom(room);
				cont1.setRoomLocked(lockChanges.isSelected()); 
			}
		}
		//end of Room methods
		
		
		//Refresh everything 
		tf.refresh();
	}


	//--------------------------------------------------------------------------
	// Removes an item (entry, judge, or room) from an items on pairings 
	// container (debate, jrass, jass, or rass). (It will thus appear on the 
	// bottom of the screen when the screen is refreshed.) 
	//--------------------------------------------------------------------------
	void removeFromIOP (Object obj, Flightable container)
	{
		//if it's an entry, remove from debate 
		if (obj instanceof Entry)
		{
			Debate debate = (Debate) container; 
		
			debate.removeTeam ((Entry) obj); 

			//if they were the only debater in that debate, then it's no longer
			//a debate - convert it to an assignment or blankline. 
			if (debate.getNumTeams() == 0)
				convertDebate (debate);
		}

		//if it's a judge, remove from judgeinhabitable 
		else if (obj instanceof Judge)
		{
			Judge judge = (Judge) obj; 
			
			//FIRST, remove the judge from its current container
			((JudgeInhabitable) container).removeJudge(judge);
			
			//If not a debate, the judge may be in a judgeassignment or 
			//judgeroomassignment. See if it was the only judge in the item. 
			if (container instanceof Debate == false)
			{
				//If it only had one judge, and change it to a roomassmnt or 
				//blankline. 
				if (((JudgeInhabitable) container).getJudges().isEmpty())
					convertToObjWithoutJudge ((JudgeInhabitable) container);
			}
		}

		//if it's a room, remove from roominhabitable
		else if (obj instanceof Room)
		{
			//First, remove from current container 
			((RoomInhabitable) container).setRoom(null); 
			
			//If not a deabte, it's either a jrass or roomassignment, so convert it
			if (container instanceof Debate == false)
				convertToObjWithoutRoom ((RoomInhabitable) container); 
		}

		tf.refresh(); 
	}



	//--------------------------------------------------------------------------
	// Converts a debate into an Assignment or BlankLine. (Will be called after 
	// all teams have been removed from the debate.) The debate will be in the 
	// itemsOnPairings arraylist. 
	//--------------------------------------------------------------------------
	private void convertDebate (Debate debate)
	{
		Flightable newContainer; 
		
		ArrayList<Judge> judges = debate.getJudges(); 
		Room room = debate.getRoom(); 
		char flight = debate.getFlight(); 
		
		if (judges.size() > 0 && room == null)
		{
			newContainer = new JudgeAssignment(judges, flight, false, round);
			
			//carry over whether the judges are locked into the new container, and 
			//remove old locks. 
			for (Judge judge : judges)
			{
				judge.setLocked((JudgeAssignment) newContainer, judge.isLocked(debate));
				judge.setLocked(debate, false);
			}
		}
			
		else if (judges.isEmpty() && room != null)
			newContainer = new RoomAssignment(room, flight, 
															debate.isRoomLocked(), round); 
		
		else if (judges.size() > 0 && room != null)
		{
			newContainer = new JudgeRoomAssignment(judges, room, flight, 
											false, debate.isRoomLocked(), round);

			//carry over whether the judges are locked into the new container, and 
			//remove old locks. 
			for (Judge judge : judges)
			{
				judge.setLocked((JudgeRoomAssignment) newContainer, 
										judge.isLocked(debate));
				judge.setLocked(debate, false);
			}
		}
					
		else //if both are null
			newContainer = new BlankLine(flight); 
		
		int i = 0; //index of the debate in itemsOnPairings 
		
		while (i < round.getItemsOnPairings().size())
		{
			if (round.getItemsOnPairings().get(i).equals(debate))
			{
				round.getItemsOnPairings().set (i, newContainer); 
				return; 
			}
			
			i++; 
		}		
	}


	//--------------------------------------------------------------------------
	// Converts an Assignment or BlankLine into a Debate object so that teams
	// may be added to it. 
	//--------------------------------------------------------------------------
	private Debate convertObjectToDebate (Flightable container)
	{
		assert container instanceof JudgeInhabitable 
			|| container instanceof RoomInhabitable
			|| container instanceof BlankLine; 
	
		Debate debate = new Debate (round); 
		round.addDebate(debate); 
		debate.setFlight(container.getFlight()); 
		
		
		if (container instanceof JudgeInhabitable)
		{
			//add all judges, and carry over their locks 
			for (Judge judge : ((JudgeInhabitable) container).getJudges())
			{
				debate.addJudge(judge);
				judge.setLocked(debate, 
								((JudgeInhabitable) container).isJudgeLocked(judge));
				judge.setLocked((JudgeInhabitable) container, false); 
			}
		}
		
		if (container instanceof RoomInhabitable)
			debate.setRoom (((RoomInhabitable) container).getRoom()); 
		
		int i = 0; //index of the container in itemsOnPairings 
		
		while (i < round.getItemsOnPairings().size())
		{
			if (round.getItemsOnPairings().get(i).equals(container))
			{
				round.getItemsOnPairings().set (i, debate); 
				break;
			}
			
			i++; 
		}
		
		return debate;
	}


	//--------------------------------------------------------------------------
	// Converts a JudgeAssignment or JudgeRoomAssignment into either a BlankLine
	// or RoomAssignment, respectively.  
	//--------------------------------------------------------------------------
	private void convertToObjWithoutJudge (JudgeInhabitable container)
	{
		//should be a JudgeInhabitable but not a Debate with entries
		assert !(container instanceof Debate); 
		
		Flightable newContainer; 
		
		if (container instanceof JudgeAssignment)
		{
			newContainer = new BlankLine (((Flightable) container).getFlight());
			newContainer.setFlight (((Flightable) container).getFlight()); 
		}
		else //if container is a JudgeRoomAssignment
			newContainer = new RoomAssignment (
				((JudgeRoomAssignment) container).getRoom(), 
				((Flightable) container).getFlight(), 
				((JudgeRoomAssignment) container).isRoomLocked(), round);
		
		int i = 0; //index of the container in itemsOnPairings 
		
		while (i < round.getItemsOnPairings().size())
		{
			if (round.getItemsOnPairings().get(i).equals(container))
			{
				round.getItemsOnPairings().set (i, newContainer); 
				return;
			}
			
			i++;
		}
	}


	//--------------------------------------------------------------------------
	// Converts a RoomAssignment or BlankLine into a JudgeRoomAssignment or 
	// JudgeAssignment, respectively. 
	//--------------------------------------------------------------------------
	private JudgeInhabitable convertToJudgeInhabitable (Flightable container)
	{
		assert container instanceof RoomAssignment 
			|| container instanceof BlankLine;
		
		JudgeInhabitable newContainer; 
		
		if (container instanceof RoomAssignment) 
			newContainer = new JudgeRoomAssignment (new ArrayList<Judge>(), 
				((RoomAssignment) container).getRoom(), container.getFlight(), 
				false, ((RoomAssignment) container).isLocked(), round); 
		
		else //if container is a BlankLine
			newContainer = new JudgeAssignment (new ArrayList<Judge>(), 
								container.getFlight(), false, round); 
		
		int i = 0; //index of the container in itemsOnPairings 
		
		while (i < round.getItemsOnPairings().size())
		{
			if (round.getItemsOnPairings().get(i).getClass() == container.getClass()
				&& round.getItemsOnPairings().get(i).equals(container))
			{
				round.getItemsOnPairings().set (i, (Flightable) newContainer); 
				break;
			}
			
			i++;
		}
		
		return newContainer; 
	}


	//--------------------------------------------------------------------------
	// Converts a RoomAssignment or JudgeRoomAssignment into either a BlankLine
	// or JudgeAssignment, respectively.  
	//--------------------------------------------------------------------------
	private void convertToObjWithoutRoom (RoomInhabitable container)
	{
		//should be a RoomInhabitable but not a Debate with entries
		assert !(container instanceof Debate); 
		
		Flightable newContainer; 
		
		if (container instanceof RoomAssignment)
		{
			newContainer = new BlankLine (((Flightable) container).getFlight());
			newContainer.setFlight (((Flightable) container).getFlight()); 
		}
		else //if container is a JudgeRoomAssignment
		{
			JudgeRoomAssignment jrass = (JudgeRoomAssignment) container; 
			
			newContainer = new JudgeAssignment (new ArrayList<Judge>(),
				jrass.getFlight(), false, round);
			
			//transfer all judges and locks 
			for (Judge judge : jrass.getJudges())
			{
				((JudgeAssignment) newContainer).addJudge(judge);
				judge.setLocked ((JudgeAssignment) newContainer, judge.isLocked(jrass));
				judge.setLocked (jrass, false);
			}
		}
		
		int i = 0; //index of the container in itemsOnPairings 
		
		while (i < round.getItemsOnPairings().size())
		{
			if (round.getItemsOnPairings().get(i) instanceof RoomInhabitable 
				&& round.getItemsOnPairings().get(i).equals(container))
			{
				round.getItemsOnPairings().set (i, newContainer); 
				return;
			}
			
			i++;
		}
	}


	//--------------------------------------------------------------------------
	// Converts a JudgeAssignment or BlankLine into a JudgeRoomAssignment or 
	// RoomAssignment, respectively. 
	//--------------------------------------------------------------------------
	private RoomInhabitable convertToRoomInhabitable (Flightable container)
	{
		assert container instanceof JudgeAssignment 
			|| container instanceof BlankLine;
		
		RoomInhabitable newContainer; 
		
		if (container instanceof JudgeAssignment) 
		{
			JudgeAssignment judgeass = (JudgeAssignment) container; 

			newContainer = new JudgeRoomAssignment (new ArrayList<Judge>(), null,
				container.getFlight(), false,	false, round); 

			//transfer all judges and locks 
			for (Judge judge : judgeass.getJudges())
			{
				((JudgeRoomAssignment) newContainer).addJudge(judge);
				judge.setLocked ((JudgeRoomAssignment) newContainer, 
																		judge.isLocked(judgeass));
				judge.setLocked (judgeass, false);
			}
		}

		
		else //if container is a BlankLine
			newContainer = new RoomAssignment (null, container.getFlight(), 
															lockChanges.isSelected(), round); 
		
		int i = 0; //index of the container in itemsOnPairings 
		
		while (i < round.getItemsOnPairings().size())
		{
			if (round.getItemsOnPairings().get(i).equals(container))
			{
				round.getItemsOnPairings().set (i, (Flightable) newContainer); 
				break;
			}
			
			i++;
		}
		
		return newContainer; 
	}


	//--------------------------------------------------------------------------
	// Returns a list of all the judges who are paired with a specific room but
	// without any debaters yet assigned. The list will then be passed to Pair
	// when pairing. 
	//--------------------------------------------------------------------------
	private ArrayList<JudgeRoomAssignment> getJudgeRoomAssignments ()
	{
		ArrayList<JudgeRoomAssignment> list 
					= new ArrayList<JudgeRoomAssignment>();
		
		for (Flightable obj : round.getItemsOnPairings())
			if (obj instanceof JudgeRoomAssignment)
				list.add ((JudgeRoomAssignment) obj);

		return list; 
	}


	//---------------------------------------------------------------------------
	// Changes the view to a BreakScreen. "Purpose" is the purpose of the 
	// BreakScreen. 
	//---------------------------------------------------------------------------
	void showBreakScreen (int purpose)
	{
		scrollPane.setViewportView (new BreakScreen(purpose, this)); 
		setJMenuBar (null); //can't view menu bar from break screen. 
		validate(); 
	}



	//---------------------------------------------------------------------------
	// Returns to the main screen; called when closing a BreakScreen or view/
	// enter ballots screen. 
	//---------------------------------------------------------------------------
	void restoreMainScreen ()
	{
		tf.refresh();
	}


	//---------------------------------------------------------------------------
	// Changes the view to the screen to set and view which entries are 
	// ineligible to break.  This can only be called from an open BreakScreen.
	//---------------------------------------------------------------------------
	void showIneligibleEntries ()
	{
		scrollPane.setViewportView (new IneligibleToBreakScreen(
									(BreakScreen) scrollPane.getViewport().getView()));
		validate(); 
	}

	
	//Accessor 
	TournamentFrame getTournamentFrame ()
	{
		return tf; 
	}



	//--------------------------------------------------------------------------
	// Listener for when user changes the round to single- or double-flighted
	//--------------------------------------------------------------------------
	private class FlightListener implements ItemListener 
	{
		public void itemStateChanged (ItemEvent event)
		{
			if (round.hasHappened())
			{
				//warn user first if round has started.
				int choice = JOptionPane.showConfirmDialog(RoundPanel.this, 
					"This round has already started. Are you sure you want to make " 
					+ "it " + (event.getStateChange() == ItemEvent.SELECTED 
					? "flighted" : "unflighted") + "?", "Change flights", 
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				
				if (choice != JOptionPane.YES_OPTION)
					return; 
			}
		
			if (event.getStateChange() == ItemEvent.SELECTED)
			{
				round.setFlighted(true);
				RoundPanel.this.initialize(tf);				
			}
			else if (event.getStateChange() == ItemEvent.DESELECTED)
			{
				round.setFlighted(false); 
				RoundPanel.this.initialize(tf);				
			}
		}
	}


	//--------------------------------------------------------------------------
	// Listener for when user locks all current debate setup 
	//--------------------------------------------------------------------------
	private class LockListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			//TODO 
			//Go through every itemsOnPairings and lock all attributes. 
		}
	}


	//--------------------------------------------------------------------------
	// Listener for when user unlocks all current debate setup 
	//--------------------------------------------------------------------------
	private class UnlockListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			//TODO 
			//Go through every itemsOnPairings and unlock all attributes. 
		}
	}


	//--------------------------------------------------------------------------
	// Listener for when user clears all current debate setup 
	//--------------------------------------------------------------------------
	private class ClearListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			//If the round has happened, can't clear  
			if (round.hasHappened())
			{
				JOptionPane.showMessageDialog(RoundPanel.this, "This round has "
					+ "already started - must\ncancel start of round before "
					+ "clearing all debates.", "Clear All", 
					JOptionPane.ERROR_MESSAGE); 
				return;
			}
			
			int choice = JOptionPane.showConfirmDialog (RoundPanel.this, 
					"Are you absolutely sure you want to\nclear all debates?", 
					"Clear All", JOptionPane.YES_NO_OPTION, 
					JOptionPane.WARNING_MESSAGE);
			
			if (choice != JOptionPane.YES_OPTION)
				return; 
			
			int numitems = round.getItemsOnPairings().size(); 
			
			for (int i = round.getItemsOnPairings().size() - 1; i >= 0; i--)
				round.getItemsOnPairings().remove(i); 
			
			for (int i = round.getDebates().size() - 1; i >= 0; i--)
				round.getDebates().remove(i);
				
			for (int i = 0; i < numitems; i++)
				addBlankLine(); 
			
			tf.refresh();
		}
	}



	//--------------------------------------------------------------------------
	// Listener for when user changes what type of information should appear at
	// the bottom of the panel - debaters, judges, or rooms.  
	//--------------------------------------------------------------------------
	private class UnassignedListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event)
		{
			Object source = event.getSource(); 
			
			if (source == unassignedDebaters)
				bottomPanelView =	UNASSIGNED_DEBATERS; 
			else if (source == unassignedJudges)
				bottomPanelView = UNASSIGNED_JUDGES; 
			else if (source == unassignedRooms)
				bottomPanelView = UNASSIGNED_ROOMS; 
			
			initialize(tf); 
		}
	}


	//--------------------------------------------------------------------------
	// Deletes a given round (can only be called on rounds that have been 
	// created but not yet paired 
	//--------------------------------------------------------------------------
	private class DeleteListener implements ActionListener 
	{
		private Round round; 
		
		//constructor 
		DeleteListener (Round r)
		{
			round = r; 
		}
	
		public void actionPerformed (ActionEvent event)
		{
			//TODO: FINISH THIS 
			
/*			int choice = JOptionPane.showConfirmDialog (HomePanel.this, 
				"Are you sure you want to delete this round (" + round.getName()
				+ ")?", "Delete round", JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.WARNING_MESSAGE); 
			
			if (choice == JOptionPane.OK_OPTION)
			{
				tournament.removeRound(round); 
				HomePanel.this.refresh();
			}
*/		}
	}
	
	
	//--------------------------------------------------------------------------
	// Panel containing checkboxes allowing the user to toggle between whether 
	// a unassigned debater should have a bye, forfeit, or be left out of the 
	// round altogether. 
	//--------------------------------------------------------------------------
	private class UnassignedDebaterOptionsPanel extends JPanel 
	{
		//Constructor 
		UnassignedDebaterOptionsPanel (Entry entry, Debate.Outcome status)
		{
			setLayout (new BoxLayout(this, BoxLayout.Y_AXIS)); 
			
			JCheckBox bye = new JCheckBox ("Bye"); 
			JCheckBox forfeit = new JCheckBox ("Forfeit"); 
			JCheckBox notCompeting = new JCheckBox (
										"<html>Not competing in<br />this round</html>");
			
			if (status == Debate.Outcome.BYE)
				bye.setSelected(true);
			else if (status == Debate.Outcome.FORFEIT)	
				forfeit.setSelected(true);
			else if (status == Debate.Outcome.NOT_COMPETING)
				notCompeting.setSelected(true); 
			
			UDOPListener listener = new UDOPListener(bye, forfeit, notCompeting, 
																	entry); 
			bye.addItemListener (listener); 
			forfeit.addItemListener (listener); 
			notCompeting.addItemListener (listener); 
			
			add (bye); 
			add (forfeit); 
			
			//only add not competing as an option in prelims 
			if (round instanceof ElimRound == false)
				add (notCompeting); 			
		}
	}
	
	
	
	//--------------------------------------------------------------------------
	// Listener for the checkboxes to select the status of debaters not assigned
	// to a particular debate. 
	//--------------------------------------------------------------------------
	private class UDOPListener implements ItemListener 
	{
		private JCheckBox bye, forfeit, notCompeting; 
		private Entry entry;
	
		//Constructor 
		UDOPListener (JCheckBox b, JCheckBox f, JCheckBox nc, Entry e)
		{
			bye = b; 
			forfeit = f; 
			notCompeting = nc; 
			entry = e; 
		}
	
		public void itemStateChanged (ItemEvent event)
		{
			Debate debate = null; //debate the entry is currently in 
			for (Debate deb : round.getPseudoDebates())
			{
				if (deb.hasEntry(entry))
				{
					debate = deb;
					break;
				}
			}
			
			//Deselect other buttons and change in the round and its debates
			if (event.getStateChange() ==	ItemEvent.SELECTED)
			{
				Object source = event.getSource(); 
				
				if (source == bye || source == forfeit)
					notCompeting.setSelected(false); 
				if (source == bye || source == notCompeting)
					forfeit.setSelected(false); 
				if (source == forfeit || source == notCompeting)
					bye.setSelected(false); 

				
				//type of pseudo debate that the debate is being changed to 
				Debate.Outcome type; 
				
				if (source == bye)
					type = Debate.Outcome.BYE;
				else if (source == forfeit)
					type = Debate.Outcome.FORFEIT;
				else // if source == notCompeting
					type = Debate.Outcome.NOT_COMPETING;
				
				//if the entry is not currently assigned, add it to a debate 
				if (debate == null)
				{
					debate = new Debate(round); 
					round.addDebate(debate);
					debate.setTeam (entry, 0);
				}
				
				//Set the outcome. 
				debate.setDecision (entry, type); 
			}
			
			//IMPORTANT NOTE: When setSelected(false) is called from another 
			//button, it will call this method on the other button. Therefore, this
			//will only be triggered if the button deselected was the button 
			//previously selected, i.e., if no buttons are now selected. 
			else if (event.getStateChange() == ItemEvent.DESELECTED 
					&& bye.isSelected() == false 
					&& forfeit.isSelected() == false
					&& notCompeting.isSelected() == false)
			{
				round.removeDebate (debate);				
			}
				
			tf.refresh();
		}
	}
	
	
	//--------------------------------------------------------------------------
	// Box from which user can edit a judge's or room's priority for the round 
	// from the unassigned judges or rooms view at the bottom of the panel. 
	//--------------------------------------------------------------------------
	private class PriorityBox extends JComboBox 
	{
		//Constructor 
		PriorityBox (Prioritizable object)
		{
			super (Priority.PriorityLevel.values()); 
			setSelectedItem (object.getPriority(round)); 
			addActionListener(new PriorityListener(object)); 
		}
	}

	
	//--------------------------------------------------------------------------
	// Listener for a PriorityBox 
	//--------------------------------------------------------------------------
	private class PriorityListener implements ActionListener 
	{
		private Prioritizable object; //may be judge or room 
	
		//Constructor 
		PriorityListener (Prioritizable p)
		{
			object = p;
		}
		
		public void actionPerformed (ActionEvent event)
		{
			PriorityBox pb = (PriorityBox) event.getSource(); 
			Priority.PriorityLevel level; 
			level = (Priority.PriorityLevel) pb.getSelectedItem(); 
			object.setPriority (round, level); 
			tf.refresh(); 
		}
	}
	

	//--------------------------------------------------------------------------
	// Listener for button to view (or) ballots that have already been entered 
	//--------------------------------------------------------------------------
	private class ViewBallotListener implements ActionListener 
	{
		private Debate debate; 
		
		//Constructor 
		ViewBallotListener (Debate d)
		{
			debate = d;
		}
	
		public void actionPerformed (ActionEvent event)
		{
			scrollPane.setViewportView (new ViewBallotScreen(debate, 
																				RoundPanel.this));
			setJMenuBar (null); //can't view menu bar from ballot screen 
			validate(); 
		}
	}



	//--------------------------------------------------------------------------
	// Updates the day whenever user types in box 
	//--------------------------------------------------------------------------
	private class DayListener implements DocumentListener 
	{
		public void insertUpdate (DocumentEvent event) 
		{
			try
			{
				round.setDay (event.getDocument().getText(0, 
														event.getDocument().getLength())); 
			}
			catch (BadLocationException e)
			{
				System.out.println("BadLocationException in "
											+ "RoundPanel.DayListener.");
			}
		}

		public void removeUpdate (DocumentEvent event)
		{
			try
			{
				round.setDay (event.getDocument().getText(0, 
														event.getDocument().getLength())); 
			}
			catch (BadLocationException e)
			{
				System.out.println("BadLocationException in "
											+ "RoundPanel.DayListener.");
			}
		}

		public void changedUpdate (DocumentEvent event) {}
	}



	//--------------------------------------------------------------------------
	// Updates the time of a flight whenever user types in box 
	//--------------------------------------------------------------------------
	private class TimeListener implements DocumentListener 
	{
		private char flight; 
		
		//Constructor 
		TimeListener (char f)
		{
			flight = f;
		}
	
		public void insertUpdate (DocumentEvent event) 
		{
			try
			{
				if (flight == 'A')
					round.setTime (event.getDocument().getText(0, 
														event.getDocument().getLength())); 
				else 
					round.setTimeFlightB (event.getDocument().getText(0, 
														event.getDocument().getLength())); 
			}
			catch (BadLocationException e)
			{
				System.out.println("BadLocationException in "
											+ "RoundPanel.TimeListener.");
			}
		}

		public void removeUpdate (DocumentEvent event)
		{
			insertUpdate (event); 
		}

		public void changedUpdate (DocumentEvent event) {}
	}



	//--------------------------------------------------------------------------
	// Updates whether the round is supposed to begin "ASAP", based on whether 
	// the checkbox is set 
	//--------------------------------------------------------------------------
	private class ASAPListener implements ItemListener 
	{
		public void itemStateChanged (ItemEvent event)
		{
			round.setASAP (((JCheckBox) event.getSource()).isSelected());
		}
	}


	//--------------------------------------------------------------------------
	// Updates the time of a flight whenever user types in box 
	//--------------------------------------------------------------------------
	private class CommentListener implements DocumentListener 
	{
		public void insertUpdate (DocumentEvent event) 
		{
			try
			{
				round.setCommentsOnPostings(event.getDocument().getText(0, 
														event.getDocument().getLength()));
			}
			catch (BadLocationException e)
			{
				System.out.println("BadLocationException in "
											+ "RoundPanel.TimeListener.");
			}
		}

		public void removeUpdate (DocumentEvent event)
		{
			insertUpdate (event); 
		}

		public void changedUpdate (DocumentEvent event) {}
	}

}
















