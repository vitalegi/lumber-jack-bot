package it.vitalegi.lumberjack.player;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LumberJackPlayerApplication implements ApplicationRunner {

	private static final int HEIGHT = 120;
	public static final Rectangle AREA_SCREENSHOT = new Rectangle(890, 310, 145, HEIGHT);
	public static final Rectangle AREA_LEFT = new Rectangle(0, 0, 20, HEIGHT);
	public static final Rectangle AREA_RIGHT = new Rectangle(105, 0, 20, HEIGHT);

	public static final int STEP = 5;
	public static final int[] WOOD_COLOR = new int[] { 161, 116, 56 };

	protected boolean enableImageLogger;

	public enum Action {
		LEFT, RIGHT, UNDEFINED;
	}

	public static void main(String[] args) {
		SpringApplication.run(LumberJackPlayerApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		double fpsArg = Double.parseDouble(args.getOptionValues("fps").get(0));
		int napIfChangeDirection = Integer.parseInt(args.getOptionValues("napIfChange").get(0));
		enableImageLogger = Boolean.parseBoolean(args.getOptionValues("logImgs").get(0));

		GrantFps fps = new GrantFps(fpsArg);
		TakeAction actionApplier = new TakeAction(napIfChangeDirection);

		for (int i = 0; i < 10000; i++) {
			long startTime = System.currentTimeMillis();
			BufferedImage image = screenshot();
			boolean isNextRopeOnLeft = isNextRopeOnLeft(image);
			boolean isNextRopeOnRight = isNextRopeOnRight(image);
			Action action = getNextAction(isNextRopeOnLeft, isNextRopeOnRight);
			actionApplier.execute(action);
			logStatus(image, isNextRopeOnLeft, isNextRopeOnRight, action);
			System.out.println("Step done in: " + (System.currentTimeMillis() - startTime) + "ms");
			fps.applyFps();
		}
	}

	protected void logStatus(BufferedImage image, boolean isNextRopeOnLeft, boolean isNextRopeOnRight, Action action) {
		String prefix = "traceImages/" + System.currentTimeMillis() + "_";
		String suffix = ".png";
		String mid = action.name();
		saveImage(image, prefix + mid + suffix);
	}

	protected Action getNextAction(boolean isNextRopeOnLeft, boolean isNextRopeOnRight) {
		if (isNextRopeOnLeft) {
			return Action.RIGHT;
		}
		if (isNextRopeOnRight) {
			return Action.LEFT;
		}
		return Action.UNDEFINED;
	}

	protected boolean isNextRopeOnLeft(BufferedImage image) {
		return containsColor(image, AREA_LEFT, STEP, WOOD_COLOR);
	}

	protected boolean isNextRopeOnRight(BufferedImage image) {
		return containsColor(image, AREA_RIGHT, STEP, WOOD_COLOR);
	}

	protected boolean containsColor(BufferedImage image, Rectangle area, int rate, int[] targetColor) {
		for (int x = (int) area.getX(); x < (int) (area.getX() + area.getWidth()); x += rate) {
			for (int y = (int) area.getY(); y < (int) (area.getY() + area.getHeight()); y += rate) {
				if (equals(getRGB(image, x, y), targetColor)) {
					saveImage(image, "traceImages/FOUND_" + System.currentTimeMillis() + "__" + x + "-" + y + ".png");
					return true;
				}
			}
		}
		return false;
	}

	protected boolean equals(int[] array1, int[] array2) {
		if (array1 == null && array2 == null) {
			return true;
		}
		if (array1 == null || array2 == null) {
			return false;
		}
		if (array1.length != array2.length) {
			return false;
		}
		for (int i = 0; i < array1.length; i++) {
			if (array1[i] != array2[i]) {
				return false;
			}
		}
		return true;
	}

	protected BufferedImage screenshot() {
		try {
			System.setProperty("java.awt.headless", "false");
			return new Robot().createScreenCapture(AREA_SCREENSHOT);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected int[] getRGB(BufferedImage image, int x, int y) {
		int clr = image.getRGB(x, y);
		int red = (clr & 0x00ff0000) >> 16;
		int green = (clr & 0x0000ff00) >> 8;
		int blue = clr & 0x000000ff;
		return new int[] { red, green, blue };
	}

	protected void saveImage(BufferedImage image, String filename) {
		try {
			if (enableImageLogger) {
				ImageIO.write(image, "png", new File(filename));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void pressKey(int key) {
		try {
			Robot r = new Robot();
			r.keyPress(key);
			r.keyRelease(key);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected class GrantFps {

		double fps;
		long lastFrameTimestamp = System.currentTimeMillis();

		public GrantFps(double fps) {
			this.fps = fps;
		}

		public void applyFps() {
			long frameDuration = (long) (1000.0 / fps);
			long nextFrame = lastFrameTimestamp + frameDuration;
			long now = System.currentTimeMillis();
			if (now >= nextFrame) {
				lastFrameTimestamp = now;
				return;
			}
			long sleepTime = nextFrame - now;
			System.out.println("Take a nap " + sleepTime);
			sleep(sleepTime);
			lastFrameTimestamp = System.currentTimeMillis();
		}
	}

	protected class TakeAction {

		protected int napIfChangeDirection;

		protected Action lastActionTaken;

		public TakeAction(int napIfChangeDirection) {
			super();
			this.napIfChangeDirection = napIfChangeDirection;
		}

		public void execute(Action newAction) {
			boolean changeDirection = changeDirection(newAction);
			switch (newAction) {
			case LEFT:
				goLeft();
				lastActionTaken = newAction;
				break;
			case RIGHT:
				goRight();
				lastActionTaken = newAction;
				break;
			case UNDEFINED:
				keepGoing();
				break;
			}
			if (changeDirection) {
				if (napIfChangeDirection > 0) {
					System.out.println("Change direction, wait " + napIfChangeDirection + "ms");
					sleep(napIfChangeDirection);
				}
			}
		}

		protected void keepGoing() {
			if (lastActionTaken == null) {
				System.out.println("Non ho idea di cosa devo fare (#1), vado a sinistra.");
				goLeft();
				return;
			}
			switch (lastActionTaken) {
			case LEFT:
				goLeft();
				break;
			case RIGHT:
				goRight();
				break;
			default:
				System.out.println("Non ho idea di cosa devo fare (#2), vado a sinistra.");
				goLeft();
			}
		}

		protected void goRight() {
			System.out.println("Right");
			pressKey(KeyEvent.VK_RIGHT);
		}

		protected void goLeft() {
			System.out.println("Left");
			pressKey(KeyEvent.VK_LEFT);
		}

		protected boolean changeDirection(Action newAction) {
			if (lastActionTaken == null) {
				return false;
			}
			if (newAction == Action.LEFT && lastActionTaken == Action.RIGHT) {
				return true;
			}
			if (newAction == Action.RIGHT && lastActionTaken == Action.LEFT) {
				return true;
			}
			return false;
		}
	}

	protected static void sleep(long sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
