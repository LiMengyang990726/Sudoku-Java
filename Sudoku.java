import java.awt.*; // Uses AWT's Layout Managers
import java.awt.event.*; // Uses AWT's Event Handlers
import javax.swing.*; // Uses Swing's Container/Components
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import java.io.*;
import java.net.URL;
import javax.sound.sampled.*;
/*
 * The Sudoku game.
 * To solve the number puzzle, each row, each column, and each of the
 * nine 3×3 sub-grids shall contain all of the digits from 1 to 9
 */

public class Sudoku extends JFrame {
	// Name-constants for the game properties
	public static final int GRID_SIZE = 9; // Size of the board
	public static final int SUBGRID_SIZE = 3; // Size of the sub-grid

	// All the sized
	public static final int CELL_SIZE = 60; // Cell width/height in pixels
	public static final int CANVAS_WIDTH = CELL_SIZE * GRID_SIZE;
	public static final int CANVAS_HEIGHT = CELL_SIZE * GRID_SIZE;

	// All the colors to show for userinput
	public static final Color OPEN_CELL_BGCOLOR = Color.YELLOW;
	public static final Color OPEN_CELL_TEXT_YES = new Color(0, 255, 0); // RGB
	public static final Color OPEN_CELL_TEXT_NO = Color.RED;
	public static final Color CELL_CORRECT = Color.GREEN;
	public static final Color CELL_INCORRECT = Color.RED;

	// Use one font only
	public static final Font FONT_NUMBERS = new Font("Monospaced", Font.BOLD, 20);
	public static final Font FONT_HINT = new Font("Monospaced", Font.BOLD, 12);

	// each textfield (continously using)
	private JTextField[][] tfCells = new JTextField[GRID_SIZE][GRID_SIZE];

	// constant that I make
	public int DIFFICULTY = 0; // to control that display() will only be shown after pressed the difficulty
								// level

	public static int rowSelectedHis = -1;
	public static int colSelectedHis = -1; // to change back the warning color because of the previous one wrong input

	public static int REST = 0; // to show in the status bar
	public int THISROUNDMASK; // to give back the number for the status bar in the reset()

	public int colR, rowR;// for checking subgrid conflict

	public int[][] puzzle = new int[GRID_SIZE][GRID_SIZE]; // to generate a new random puzzle
	private boolean[][] masks = new boolean[GRID_SIZE][GRID_SIZE]; // to generate a new random mask

	public JLabel statusLabel = new JLabel(); // only in this way, display() and inputListener can both see it

	public static final Color[][] NUMBER = {
			{ new Color(204, 255, 255), new Color(255, 255, 204), new Color(255, 204, 153), new Color(255, 204, 204),
					new Color(255, 204, 204), new Color(204, 204, 204), new Color(255, 255, 255),
					new Color(204, 255, 153), new Color(153, 255, 153) },
			{ new Color(255, 182, 193), new Color(255, 192, 203), new Color(255, 240, 245), new Color(255, 105, 180),
					new Color(255, 20, 147), new Color(199, 21, 133), new Color(255, 218, 185),
					new Color(255, 228, 225), new Color(188, 143, 143) },
			{ new Color(100, 149, 237), new Color(176, 196, 222), new Color(30, 144, 255), new Color(255, 250, 240),
					new Color(136, 206, 250), new Color(65, 105, 225), new Color(250, 235, 215),
					new Color(135, 206, 235), new Color(173, 216, 230) }, }; // colors for the three themes
	public int THEME = 0; // to control that display() will only be shown after choosing the theme
	public int colorControl = 0; // to choose the theme

	public int[][] USERHIS = new int[GRID_SIZE][GRID_SIZE];// for changing back all of them for reset()

	public static int SCORE = 0;
	public static int SCOREHIS = 0;
	
	public static int MUSICSTATE=0;

	/*
	 * Constructor: to setup the game and the UI Components
	 */
	public Sudoku() {
		generate();// call to generate the puzzle
		theme();
	}

	/*
	 * Method: generate the random puzzle
	 */
	public void generate() {
		int mainJumper = 1, control = 1;
		Random rn = new Random();
		// generate the same one every time
		for (int row = 0; row < GRID_SIZE; ++row) {
			mainJumper = control;
			for (int col = 0; col < GRID_SIZE; ++col) {
				if (mainJumper <= 9) {
					puzzle[row][col] = mainJumper;
					mainJumper++;
				} else {
					mainJumper = 1;
					puzzle[row][col] = mainJumper;
					mainJumper++;
				}
			}
			control = mainJumper + 3; // keep that there is not conflict with in a subgrid
			if (mainJumper == 10)
				control = 4;
			if (control > 9)
				control = (control % 9) + 1;
		}

		// interchange two rows (random rows)
		for (int row = 0; row < 9; ++row) {

			int changeRow1 = rn.nextInt(3);
			int changeRow2 = 0;
			switch (changeRow1) {
			case 2: {
				changeRow2 = rn.nextInt(2);
				break;
			}
			case 1: {
				changeRow2 = rn.nextInt(2);
				if (changeRow2 == 1)
					changeRow2 = 2;
				break;
			}
			case 0: {
				changeRow2 = rn.nextInt(2) + 1;
				break;
			}
			}

			changeRow1 += row;
			changeRow2 += row;

			for (int col = 0; col < 9; ++col) {
				int temp = puzzle[changeRow1][col];
				puzzle[changeRow1][col] = puzzle[changeRow2][col];
				puzzle[changeRow2][col] = temp;
			}
			row = row + 2;
		}

		// interchange two columns (random columns)
		for (int col = 0; col < 9; ++col) {

			int changeCol1 = rn.nextInt(3);
			int changeCol2 = 0;
			switch (changeCol1) {
			case 2: {
				changeCol2 = rn.nextInt(2);
				break;
			}
			case 1: {
				changeCol2 = rn.nextInt(2);
				if (changeCol2 == 1)
					changeCol2 = 2;
				break;
			}
			case 0: {
				changeCol2 = rn.nextInt(2) + 1;
				break;
			}
			}

			changeCol1 += col;
			changeCol2 += col;

			for (int row = 0; row < 9; ++row) {
				int temp = puzzle[row][changeCol1];
				puzzle[row][changeCol1] = puzzle[row][changeCol2];
				puzzle[row][changeCol2] = temp;
			}
			col = col + 2;
		}

		// change two big rows(random)
		int changeRowPart1 = rn.nextInt(3);
		int changeRowPart2 = rn.nextInt(3);
		for (int row = 0; row <= 2; ++row) {
			for (int col = 0; col < GRID_SIZE; ++col) {
				int temp = puzzle[row + (changeRowPart1 * 3)][col];
				puzzle[row + (changeRowPart1 * 3)][col] = puzzle[row + (changeRowPart2 * 3)][col];
				puzzle[row + (changeRowPart2 * 3)][col] = temp;
			}
		}

		// change two big columns(random)
		int changeColPart1 = rn.nextInt(3);
		int changeColPart2 = rn.nextInt(3);
		for (int col = 0; col <= 2; ++col) {
			for (int row = 0; row < GRID_SIZE; ++row) {
				int temp = puzzle[row][col + (changeColPart1 * 3)];
				puzzle[row][col + (changeColPart1 * 3)] = puzzle[row][col + (changeColPart2 * 3)];
				puzzle[row][col + (changeColPart2 * 3)] = temp;
			}
		}

	}


	/*
	 * Method: for the user to choose theme
	 */
	public void theme() {
		JFrame haha = new JFrame();
		haha.setLayout(new FlowLayout());
		JPanel p1 = new JPanel();
		JPanel p2 = new JPanel();
		JButton btn1 = new JButton("Fresh Breeze");
		JButton btn2 = new JButton("Coral Incandescent");
		JButton btn3 = new JButton("Icy Frozen");
		JLabel meg = new JLabel("Choose a theme for your Sudoku:");

		haha.add(p1);
		haha.add(p2);
		haha.setVisible(true);
		haha.setSize(400, 150);
		haha.setLocation(600, 400);

		p1.add(meg);
		p2.add(btn1);
		p2.add(btn2);
		p2.add(btn3);

		btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				THEME = 0;
				colorControl = 1;
				difficulty();
				haha.setVisible(false);
			}
		});
		btn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				THEME = 1;
				colorControl = 1;
				difficulty();
				haha.setVisible(false);
			}
		});
		btn3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				THEME = 2;
				colorControl = 1;
				difficulty();
				haha.setVisible(false);
			}
		});
	}

	/*
	 * Method: to generate mask according to the difficulty level
	 */
	public void difficulty() {
		JFrame hehe = new JFrame();
		hehe.setLayout(new FlowLayout());
		JPanel p1 = new JPanel();
		JPanel p2 = new JPanel();
		hehe.add(p1);
		hehe.add(p2);
		hehe.setVisible(true);
		hehe.setSize(350, 150);
		hehe.setTitle("Home Page");

		JLabel meg = new JLabel();
		meg.setText("Choose the following difficulty level");
		p1.add(meg);

		JButton btn1 = new JButton();
		btn1.setText("Easy");
		btn1.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				DIFFICULTY = 1;
				easyLevel();
				display();
				hehe.setVisible(false);
			}
		});
		JButton btn2 = new JButton();
		btn2.setText("Medium");
		btn2.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				DIFFICULTY = 1;
				mediumLevel();
				display();
				hehe.setVisible(false);
			}
		});
		JButton btn3 = new JButton();
		btn3.setText("Hard");
		btn3.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				DIFFICULTY = 1;
				hardLevel();
				display();
				hehe.setVisible(false);
			}
		});

		p2.add(btn1);
		p2.add(btn2);
		p2.add(btn3);

	}

	/*
	 * Method: mask of easyLevel
	 */
	public void easyLevel() {
		Random rn = new Random();

		for (int row = 0; row < GRID_SIZE; ++row) {
			int hcount = 0;
			int vcount = 0;
			boolean flag = true;
			for (int col = 0; (col < GRID_SIZE) && flag; ++col) {
				int temp = (int) (rn.nextInt(10) % 2);
				if (temp == 1) {
					masks[row][col] = true;
					REST++;
					hcount++;
					for (int k = 0; k < row; ++k) {
						if (masks[k][col])
							vcount++;
					}
				} else {
					masks[row][col] = false;
				}

				if ((hcount == 1) || (vcount == 1)) { // still got logic error, all the masks tend to concentrate in
														// middle east
					flag = false;
				}
			}
		}
		THISROUNDMASK = REST;
	}

	/*
	 * Method: mask of mediumLevel
	 */
	public void mediumLevel() {
		Random rn = new Random();

		for (int row = 0; row < GRID_SIZE; ++row) {
			int hcount = 0;
			int vcount = 0;
			boolean flag = true;
			for (int col = 0; (col < GRID_SIZE) && flag; ++col) {
				int temp = (int) (rn.nextInt(10) % 2);
				if (temp == 1) {
					masks[row][col] = true;
					REST++;
					hcount++;
					for (int k = 0; k < row; ++k) {
						if (masks[k][col])
							vcount++;
					}
				} else {
					masks[row][col] = false;
				}

				if ((hcount == 3) || (vcount == 3)) {
					flag = false;
				}
			}
		}
		THISROUNDMASK = REST;
	}

	/*
	 * Method: mask of hardLevel
	 */
	public void hardLevel() {
		Random rn = new Random();

		for (int row = 0; row < GRID_SIZE; ++row) {
			int hcount = 0;
			int vcount = 0;
			boolean flag = true;
			for (int col = 0; (col < GRID_SIZE) && flag; ++col) {
				int temp = (int) (rn.nextInt(10) % 2);
				if (temp == 1) {
					masks[row][col] = true;
					REST++;
					hcount++;
					for (int k = 0; k < row; ++k) {
						if (masks[k][col])
							vcount++;
					}
				} else {
					masks[row][col] = false;
				}

				if ((hcount == 5) || (vcount == 5)) {
					flag = false;
				}
			}
		}
		THISROUNDMASK = REST;
	}

	/*
	 * Method: Main display UI
	 */
	public void display() {
		JFrame overall = new JFrame();
		overall.setLayout(new BorderLayout());

		Container cp = getContentPane();
		cp.setLayout(new GridLayout(GRID_SIZE, GRID_SIZE));
		overall.add(cp, BorderLayout.CENTER);

		JPanel statusBar = new JPanel();
		overall.add(statusBar, BorderLayout.SOUTH);

		overall.setSize(new Dimension(CANVAS_WIDTH + 300, CANVAS_HEIGHT + 80));
		overall.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Handle window closing
		overall.setTitle("Sudoku");
		overall.setVisible(true);

		// statusBar
		statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusBar.setPreferredSize(new Dimension(CANVAS_WIDTH + 300, 30));
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusBar.add(statusLabel);

		// SidePanel
		JPanel sidePanel = new JPanel();
		overall.add(sidePanel, BorderLayout.EAST);
		JButton hint = new JButton("Hint");
		JLabel lbhint = new JLabel("This will deduct one mark");
		lbhint.setFont(FONT_HINT);
		JLabel lbcheat = new JLabel("Reveal cells");
		lbcheat.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbcheatmeg1 = new JLabel("This will deduct coresponding marks.");
		JLabel lbcheatmeg2 = new JLabel("If your score is smaller than the number");
		JLabel lbcheatmeg3 = new JLabel("you type in, answer will not be given");
		lbcheatmeg1.setFont(FONT_HINT);
		lbcheatmeg2.setFont(FONT_HINT);
		lbcheatmeg3.setFont(FONT_HINT);
		JTextField tfcheat = new JTextField();
		JButton btnsound = new JButton("Sound");
		// JButton pause = new JButton("Pause");
		// JButton resume = new JButton("Resume");
		sidePanel.setLayout(new GridLayout(11, 1));
		sidePanel.setPreferredSize(new Dimension(300, CANVAS_HEIGHT));
		sidePanel.add(new JPanel());
		sidePanel.add(hint);
		sidePanel.add(lbhint);
		sidePanel.add(new JPanel());
		sidePanel.add(lbcheat);
		sidePanel.add(lbcheatmeg1);
		sidePanel.add(lbcheatmeg2);
		sidePanel.add(lbcheatmeg3);
		sidePanel.add(tfcheat);
		sidePanel.add(btnsound);
		// sidePanel.add(pause);
		// sidePanel.add(resume);
		btnsound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(MUSICSTATE==0) {
					music m1 = new music();
					m1.main(null);
					MUSICSTATE=1;
				}else if(MUSICSTATE==1) {
					disableMusic dm1 = new disableMusic();
					dm1.main(null); //currently cannot work
				}
			}
		});
		hint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean flag = true;
				if(SCORE >= 1) {
					for (int row = 8; (row >= 0 && flag); --row) {
						for (int col = 8; (col >= 0 && flag); --col) {
							if (masks[row][col] == true) {
								masks[row][col] = false;
								tfCells[row][col].setBackground(CELL_CORRECT);
								tfCells[row][col].setText("" + puzzle[row][col]);
								REST--;
								USERHIS[row][col] = 1;
								SCORE--;
								flag = false;
								statusLabel.setText(
										"Status: You have " + REST + " left, and your current score is: " + SCORE + " .");
								// check whether need to restart
								if (REST == 0) {
									new next();
								}
							}
						}
					}
				}
			}
		});
		tfcheat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextField source = (JTextField) e.getSource();
				int number = Integer.parseInt(tfcheat.getText());
				int num = 0;
				if (SCORE > number) {
					for (int row = 8; (row >= 0 && num <= number); --row) {
						for (int col = 8; (col >= 0 && num <= number); --col) {
							if (masks[row][col] == true) {
								masks[row][col] = false;
								tfCells[row][col].setBackground(CELL_CORRECT);
								tfCells[row][col].setText("" + puzzle[row][col]);
								REST--;
								USERHIS[row][col] = 1;
								num++;
								SCORE = SCORE - number;
								statusLabel.setText("Status: You have " + REST + " left, and your current score is: "
										+ SCORE + " .");
								// check whether need to restart
								if (REST == 0) {
									new next();
								}
							}
						}
					}
				}
			}
		});

		// Menu part
		JMenuBar MenuBar = new JMenuBar();
		JMenu JMenu1 = new JMenu("File");
		JMenuItem JMenuItem1 = new JMenuItem("New Game");
		JMenuItem JMenuItem2 = new JMenuItem("Reset Game");
		JMenuItem JMenuItem3 = new JMenuItem("Exit");
		JMenuItem1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Sudoku();
			}
		});
		JMenuItem2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new reset();
			}
		});
		JMenuItem3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		JMenu1.add(JMenuItem1);
		JMenu1.add(JMenuItem2);
		JMenu1.add(JMenuItem3);

		JMenu JMenu2 = new JMenu("Options");
		JMenuItem JMenuItem4 = new JMenuItem("Easy");
		JMenuItem JMenuItem5 = new JMenuItem("Medium");
		JMenuItem JMenuItem6 = new JMenuItem("Hard");
		JMenuItem4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Sudoku();
				easyLevel();
			}
		});
		JMenuItem5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Sudoku();
				mediumLevel();
			}
		});
		JMenuItem6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Sudoku();
				hardLevel();
			}
		});
		JMenu2.add(JMenuItem4);
		JMenu2.add(JMenuItem5);
		JMenu2.add(JMenuItem6);

		JMenu JMenu3 = new JMenu("Help");
		JMenuItem JMenuItem7 = new JMenuItem("Techniques");
		JMenu3.add(JMenuItem7);

		JMenuItem7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String url = "https://www.kristanix.com/sudokuepic/sudoku-solving-techniques.php";
					java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		});

		MenuBar.add(JMenu1);
		MenuBar.add(JMenu2);
		MenuBar.add(JMenu3);
		overall.add(MenuBar);
		overall.setJMenuBar(MenuBar);

		// Main display for the Sudoku
		if ((DIFFICULTY == 1) && (colorControl == 1)) {
			cp.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
			pack();

			InputListener listener = new InputListener();
			for (int row = 0; row < GRID_SIZE; ++row) {
				for (int col = 0; col < GRID_SIZE; ++col) {
					tfCells[row][col] = new JTextField(); // Allocate element of array
					cp.add(tfCells[row][col]); // ContentPane adds JTextField
					if (masks[row][col]) {
						tfCells[row][col].setText(""); // set to empty string
						tfCells[row][col].setEditable(true);
						tfCells[row][col].setBackground(OPEN_CELL_BGCOLOR);
						tfCells[row][col].addActionListener(listener);

					} else {
						tfCells[row][col].setText(puzzle[row][col] + "");
						tfCells[row][col].setEditable(false);
					}

					// Beautify all the cells
					tfCells[row][col].setHorizontalAlignment(JTextField.CENTER);
					tfCells[row][col].setFont(FONT_NUMBERS);

					// change the color
					if (!masks[row][col]) {
						tfCells[row][col].setBackground(NUMBER[THEME][puzzle[row][col] - 1]);
					}

					// set the border
					if ((row == 2 || row == 5) && (col != 5 || col != 2)) {
						tfCells[row][col].setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.BLACK));
					}
					if (row == 3 || row == 6) {
						tfCells[row][col].setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, Color.BLACK));
					}
					if ((col == 2 || col == 5) && (row != 5 || row != 2)) {
						tfCells[row][col].setBorder(BorderFactory.createMatteBorder(0, 0, 0, 3, Color.BLACK));
					}
					if (col == 3 || col == 6) {
						tfCells[row][col].setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, Color.BLACK));
					}
					if ((row == 2 || row == 5) && (col == 2 || col == 5)) {
						tfCells[row][col].setBorder(BorderFactory.createMatteBorder(0, 0, 3, 3, Color.BLACK));
					}
					if ((row == 3 || row == 6) && (col == 2 || col == 5)) {
						tfCells[row][col].setBorder(BorderFactory.createMatteBorder(3, 0, 0, 3, Color.BLACK));
					}
					if ((row == 2 || row == 5) && (col == 3 || col == 6)) {
						tfCells[row][col].setBorder(BorderFactory.createMatteBorder(0, 3, 3, 0, Color.BLACK));
					}
					if ((row == 3 || row == 6) && (col == 3 || col == 6)) {
						tfCells[row][col].setBorder(BorderFactory.createMatteBorder(3, 3, 0, 0, Color.BLACK));
					}
				}
			}
		}

	}

	/*
	 * Method: to check vertical conflict
	 */
	public boolean checkVerticalConflict(int inPut, int rowSelected, int colSelected) {
		for (int row = 0; row < rowSelected; ++row) {
			if ((puzzle[row][colSelected] == inPut) && (!masks[row][colSelected])) {
				return false;
			}
		}
		for (int row = (rowSelected + 1); row < GRID_SIZE; ++row) {
			if ((puzzle[row][colSelected] == inPut) && (!masks[row][colSelected])) {
				return false;
			}
		}
		return true;
	};

	/*
	 * Method: check horizontal conflict
	 */
	public boolean checkHorizontalConflict(int inPut, int rowSelected, int colSelected) {
		for (int col = 0; col < colSelected; ++col) {
			if ((puzzle[rowSelected][col] == inPut) && (!masks[rowSelected][col])) {
				return false;
			}
		}
		for (int col = (colSelected + 1); col < GRID_SIZE; ++col) {
			if ((puzzle[rowSelected][col] == inPut) && (!masks[rowSelected][col])) {
				return false;
			}
		}
		return true;
	};

	/*
	 * Method: check subgrid conflict
	 */
	public boolean checkSubgridConflict(int inPut, int rowSelected, int colSelected) {
		colR = colSelected % 3;
		rowR = rowSelected % 3;
		for (int row = rowSelected - rowR; row <= (rowSelected + (2 - rowR)); ++row) {
			for (int col = colSelected - colR; col <= (colSelected + (2 - colR)); ++col) {
				if ((puzzle[row][col] == inPut) && (row != rowSelected) && (col != colSelected) && (!masks[row][col])) {
					return false;
				}
			}
		}
		return true;
	}

	/*
	 * Inner class: actionListener for reacting user input to the puzzle
	 */
	public class InputListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// All the 9*9 JTextFileds invoke this handler. We need to determine
			// which JTextField (which row and column) is the source for this invocation.
			int rowSelected = -1;
			int colSelected = -1;

			// Get the source object that fired the event
			JTextField source = (JTextField) e.getSource();
			// Scan JTextFileds for all rows and columns, and match with the source object
			boolean found = false;
			for (int row = 0; row < GRID_SIZE && !found; ++row) {
				for (int col = 0; col < GRID_SIZE && !found; ++col) {
					if (tfCells[row][col] == source) {
						rowSelected = row;
						colSelected = col;
						found = true; // break the inner/outer loops
					}
				}
			}

			// check for valid input
			String input = tfCells[rowSelected][colSelected].getText();
			int inPut = -1;
			boolean isNumeric = input.chars().allMatch(Character::isDigit);
			if (isNumeric) {
				inPut = Integer.parseInt(input);
			} else {
				wrongInput();
			}

			boolean c1 = checkVerticalConflict(inPut, rowSelected, colSelected);
			boolean c2 = checkHorizontalConflict(inPut, rowSelected, colSelected);
			boolean c3 = checkSubgridConflict(inPut, rowSelected, colSelected);

			// go back to the normal color (History)
			if ((rowSelectedHis != -1) && (colSelectedHis != -1)) {
				boolean c1His = checkVerticalConflict(inPut, rowSelectedHis, colSelectedHis);
				boolean c2His = checkHorizontalConflict(inPut, rowSelectedHis, colSelectedHis);
				boolean c3His = checkSubgridConflict(inPut, rowSelectedHis, colSelectedHis);

				if (c1His) {
					for (int row = 0; row < GRID_SIZE; ++row) {
						if (masks[row][colSelected]) {
							tfCells[row][colSelected].setBackground(OPEN_CELL_BGCOLOR);
						} else if (row != rowSelected) {
							tfCells[row][colSelected].setBackground(NUMBER[THEME][puzzle[row][colSelected] - 1]);
						}
					}
				}

				if (c2His) {
					for (int col = 0; col < GRID_SIZE; ++col) {
						if (masks[rowSelected][col]) {
							tfCells[rowSelected][col].setBackground(OPEN_CELL_BGCOLOR);
						} else if (col != colSelected) {
							tfCells[rowSelected][col].setBackground(NUMBER[THEME][puzzle[rowSelected][col] - 1]);
						}
					}
				}

				if (c3His) {
					for (int row = rowSelectedHis - rowR; row <= (rowSelectedHis + (2 - rowR)); ++row) {
						for (int col = colSelectedHis - colR; col <= (colSelectedHis + (2 - colR)); ++col) {
							if (masks[row][col]) {
								tfCells[row][col].setBackground(OPEN_CELL_BGCOLOR);
							} else if ((row == rowSelected) && (col == colSelected)) {
								tfCells[row][col].setBackground(CELL_CORRECT);
							} else {
								tfCells[row][col].setBackground(NUMBER[THEME][puzzle[row][col] - 1]);
							}
						}
					}
				}
			}

			// check for the current one whether it is correct or not
			if (c1) {
				masks[rowSelected][colSelected] = false;
			} else {
				for (int row = 0; row < GRID_SIZE; ++row) {
					tfCells[row][colSelected].setBackground(CELL_INCORRECT);
				}
			}

			if (c2) {
				masks[rowSelected][colSelected] = false;
			} else {
				for (int col = 0; col < GRID_SIZE; ++col) {
					tfCells[rowSelected][col].setBackground(CELL_INCORRECT);
				}
			}

			if (c3) {
				masks[rowSelected][colSelected] = false;
			} else {
				for (int row = rowSelected - rowR; row <= (rowSelected + (2 - rowR)); ++row) {
					for (int col = colSelected - colR; col <= (colSelected + (2 - colR)); ++col) {
						tfCells[row][col].setBackground(CELL_INCORRECT);
					}
				}
				tfCells[rowSelected][colSelected].setBackground(CELL_INCORRECT);
			}

			rowSelectedHis = rowSelected;
			colSelectedHis = colSelected;

			// if there is no conflict under three cases, do the following
			if (c1 && c2 && c3) {
				REST--;
				tfCells[rowSelected][colSelected].setBackground(CELL_CORRECT);
				USERHIS[rowSelected][colSelected] = 1;
				SCORE++;
			}

			// check whether need to restart
			if (REST == 0) {
				new next();
			}

			// update the statusbar
			SCORE += SCOREHIS;
			statusLabel.setText("Status: You have " + REST + " left, and your current score is: " + SCORE + " .");
		}
	}

	/*
	 * Inner class: show wrong input
	 */
	public void wrongInput() {
		JFrame wrongWindow = new JFrame();
		wrongWindow.setLayout(new FlowLayout());

		JLabel wrongMeg = new JLabel("Invalid input!");

		wrongWindow.add(wrongMeg);
		wrongWindow.setSize(200, 50);
		wrongWindow.setVisible(true);
		wrongWindow.setLocation(600, 400);
	}

	/*
	 * Inner class: generate the restart UI
	 */
	public class restart extends JFrame {
		private JPanel p1;
		private JPanel p2;
		private JLabel meg;
		private JButton button1;
		private JButton button2;

		public restart() {
			setLayout(new FlowLayout());

			p1 = new JPanel();
			p2 = new JPanel();
			add(p1);
			add(p2);

			button1 = new JButton();
			button2 = new JButton();
			meg = new JLabel();
			p2.add(button1);
			p2.add(button2);
			p1.add(meg);
			button1.setText("Yes");
			button2.setText("No");
			meg.setText("Do you want to continue with this game?");

			setVisible(true);
			setSize(350, 200);
			setLocation(600, 400);

			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					REST = 0;
					DIFFICULTY = 0;
					rowSelectedHis = -1;
					colSelectedHis = -1;
					new Sudoku();
				}
			});
			button2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
		}
	}

	/*
	 * Inner class: reset UI
	 */
	public class reset extends JFrame {
		public reset() {
			for (int row = 0; row < GRID_SIZE; ++row) {
				for (int col = 0; col < GRID_SIZE; ++col) {
					if (USERHIS[row][col] == 1) {
						masks[row][col] = true;
						tfCells[row][col].setBackground(OPEN_CELL_BGCOLOR);
						tfCells[row][col].setText("");
					} else if (tfCells[row][col].getBackground() == CELL_INCORRECT) {
						tfCells[row][col].setBackground(NUMBER[THEME][puzzle[row][col] - 1]);
					}
				}
			}
			REST = THISROUNDMASK;
			SCORE = 0;
			statusLabel.setText("Status: You have " + REST + " left, and your current score is: " + SCORE + " .");
		}
	}

	/*
	 * Inner class: next game UI, according to the score setting
	 */
	public class next extends JFrame {
		public next() {
			JFrame next = new JFrame();
			JPanel p1 = new JPanel();
			JPanel p2 = new JPanel();
			JLabel meg = new JLabel("Congratulation! Do you want to continue with the current score?");
			JButton btn1 = new JButton("Yes");
			JButton btn2 = new JButton("No");

			next.setLayout(new FlowLayout());
			next.setVisible(true);
			next.setSize(new Dimension(500, 100));
			next.setLocation(300, 300);
			next.add(p1);
			next.add(p2);

			p1.add(meg);
			p2.add(btn1);
			p2.add(btn2);
			btn1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SCOREHIS = SCORE;
					SCORE = 0;
					new Sudoku();
					next.setVisible(false);
				}
			});
			btn2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SCORE = 0;
					new Sudoku();
				}
			});
		}
	}

	/*
	 * Method:The entry main
	 */
	public static void main(String[] args) {
		new Sudoku();
	}
}