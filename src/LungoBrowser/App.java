package LungoBrowser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
// import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import LungoBrowser.Debug.Flag;

import static LungoBrowser.Debug.Log;
// import static LungoBrowser.Debug.Warn;
// import static LungoBrowser.Debug.Error;

import java.util.Map.Entry;

public class App {

	private static HashMap<Integer, Window> IDs = new HashMap<>();

	private static ArrayList<Window> windows = new ArrayList<Window>();

	public final static String AppName = "Lungo";

	public final static Version ver = new Version("A1.1.1");

	public static void main(String[] args) throws Exception {

		var AllowedFlags = new ArrayList<Flag>();
		AllowedFlags.add(new Flag("link|l", "will open the link, args: $link", 1));
		AllowedFlags.add(new Flag("box|b", "will show hitboxes", 0));
		AllowedFlags.add(new Flag("size|s", "will change the size, args: $width $height", 2));
		AllowedFlags.add(new Flag("max|f", "will maximize application", 0));
		AllowedFlags.add(new Flag("snap|sn", "will take a screenshot, needs size and link flag", 1));

		Debug.createFlags(AllowedFlags);

		Debug.setFlags(args);

		// takes a screenshot of a webpage from the drawr. useful for taking a
		// screenshot and comparing it with the desired output of the specification. and
		// doing screenshots in general :^)
		if (Debug.ifFlag("snap") && Debug.ifFlag("size") && Debug.ifFlag("link")) {
			var snapDrawr = new Drawer(null, null);
			var sizeVals = Debug.getFlagValue("size");
			var snapVals = Debug.getFlagValue("snap");
			var linkVals = Debug.getFlagValue("link");
			if (sizeVals.size() > 1 && snapVals.size() > 0 && linkVals.size() > 0) {
				var width = Integer.parseInt(sizeVals.get(0));
				var height = Integer.parseInt(sizeVals.get(1));

				var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

				snapDrawr.UrlUpdated(new URI(linkVals.get(0)));
				snapDrawr.Paint(new Dimension(width, height), image.getGraphics());

				File outputFile = new File(snapVals.get(0));
				ImageIO.write(image, "jpg", outputFile);
			}
			System.exit(0);
		}

		if (Debug.ifFlag("help")) // we don't want to continue if the help command was triggered
			System.exit(0);

		var DefUrl = ""; // url to load the main window with

		if (Debug.ifFlag("link")) {
			var vals = Debug.getFlagValue("link");
			if (vals.size() > 0) {
				DefUrl = vals.get(0);
			}
		}

		var main = CreateWindow(DefUrl);

		if (Debug.ifFlag("size") && !Debug.ifFlag("max")) {
			var vals = Debug.getFlagValue("size");
			if (vals.size() > 1) {
				var width = vals.get(0);
				var height = vals.get(1);
				main.DefaultSize = new Dimension(Integer.parseInt(width), Integer.parseInt(height));
			}
		}

		if (Debug.ifFlag("max")) {
			main.isFullScreen = true;
		}

		main.start();
		Log("Started browser");
	}

	public static void RemoveWindow(Window window) { // removes a window with the corresponding id
		for (int i = 0; i < windows.size(); i++) {
			if (windows.get(i).equals(window)) {
				windows.remove(i);
				IDs.remove(getWindowID(window));
			}
		}
		if (windows.size() == 0) { // exits the browser if no windows exist
			Log("no Window Threads now exiting...");
			System.exit(0);
		}
	}

	public static int findUnusedInteger(ArrayList<Integer> array) { // returns and id that isn't used yet
		// Create a set to store used integers
		Set<Integer> usedIntegers = new HashSet<>();

		// Iterate through the array and add each integer to the set
		for (int num : array) {
			usedIntegers.add(num);
		}

		// Find the first unused integer starting from 0
		int unusedInteger = 0;
		while (usedIntegers.contains(unusedInteger)) {
			unusedInteger++;
		}

		return unusedInteger;
	}

	public static Window CreateWindow(String url) { // creates a window and a corresponding id
		Window window = new Window();

		int windowID = findUnusedInteger(new ArrayList<>(IDs.keySet()));

		IDs.put(windowID, window);

		windows.add(window);

		window.LoadedUrl = url;
		return window;
	}

	public static int getWindowID(Window window) { // gets the window id
		int val = -1;
		for (Entry<Integer, Window> i : new ArrayList<>(IDs.entrySet())) {
			if (i.getValue().equals(window))
				val = i.getKey();
		}
		return val;
	}

	public static Image GetImage(String url) { // get image FROM THE JAR FILE!!!
		var url2Img = App.class.getClassLoader().getResource(url);

		if (url2Img == null) {
			new Exception("Unable to load image: " + url).printStackTrace();
		}

		return new ImageIcon(url2Img).getImage();
	}

	public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) // resize an
																											// image
			throws IOException {
		Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
		BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
		return outputImage;
	}

	public static BufferedImage GetImageFromData(byte[] bytes) { // converts byte array to BufferedImage
		try {
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
			return image;
		} catch (IOException e) {
			// BufferedImage image = (new ByteArrayInputStream(bytes));
			Debug.Error(e.getCause());
			return null;
		}
	}

	public static BufferedImage toBufferedImage(Image img) { // converts an image to buffered image if not already
		if (img instanceof BufferedImage) { // return if already buffered image
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics bGr = bImage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bImage;
	}

	public static Dimension getDimensionOfImage(Image image) { // gets the dimensions of the image
		return new Dimension(image.getWidth(null), image.getHeight(null));
	}

	public static BufferedImage fitImageWithinBounds(int maxWidth, int maxHeight, BufferedImage img) { // resizes the
																										// image to keep
																										// the aspect
																										// ratio while
																										// fitting
																										// inside the
																										// maxwidth/-height
		Dimension imageSize = App.getDimensionOfImage(img);
		double aspectRatio = (double) imageSize.width / imageSize.height;
		int width = imageSize.width;
		int height = imageSize.height;

		// Adjust width if it exceeds maxWidth
		if (width > maxWidth) {
			width = maxWidth;
			height = (int) (width / aspectRatio);
		}

		// Adjust height if it exceeds maxHeight
		if (height > maxHeight) {
			height = maxHeight;
			width = (int) (height * aspectRatio);
		}

		int xOffset = (maxWidth - width) / 2;
		int yOffset = (maxHeight - height) / 2;

		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = resizedImage.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.drawImage(img, 0, 0, width, height, null);
		g2d.dispose();

		BufferedImage finalImage = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D gFinal = finalImage.createGraphics();
		gFinal.setBackground(new Color(0, 0, 0, 0)); // Set background color to transparent
		gFinal.clearRect(0, 0, maxWidth, maxHeight); // Clear the background
		gFinal.drawImage(resizedImage, xOffset, yOffset, null);
		gFinal.dispose();

		return finalImage;
	}

	public static BufferedImage coverImageWithinBounds(int maxWidth, int maxHeight, BufferedImage img) {
		Dimension imageSize = App.getDimensionOfImage(img);
		double aspectRatio = (double) imageSize.width / imageSize.height;
		double targetAspectRatio = (double) maxWidth / maxHeight;

		int width;
		int height;
		int x = 0;
		int y = 0;

		if (aspectRatio > targetAspectRatio) {
			height = maxHeight;
			width = (int) (height * aspectRatio);
			x = (width - maxWidth) / 2;
		} else {
			width = maxWidth;
			height = (int) (width / aspectRatio);
			y = (height - maxHeight) / 2;
		}

		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = resizedImage.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.drawImage(img, -x, -y, width, height, null);
		g2d.dispose();

		BufferedImage croppedImage = resizedImage.getSubimage(x / 2, y / 2,
				maxWidth, maxHeight);

		return croppedImage;
	}

	public static ArrayList<Byte> getFileContents(String path) {
		Path Path = Paths.get(path);

		try {
			var content = Files.readAllBytes(Path);
			var convertedContent = new ArrayList<Byte>();
			for (var elm : content)
				convertedContent.add(Byte.valueOf(elm));
			return convertedContent;
		} catch (IOException e) {
		}
		return new ArrayList<Byte>();
	}

	public static InputStream getFileInputStream(String path) throws IOException, URISyntaxException {
		return App.class.getClassLoader().getResourceAsStream(path);
	}

	public static void writeToFile(String fileName, String content) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		writer.write(content);
		writer.close();
	}

	public static void appendToFile(String fileName, String content) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
		writer.write(content);
		writer.close();
	}

	public static boolean isLocalFileURI(URI uri) {
		// Log(uri);
		if (uri.isAbsolute() && "file".equalsIgnoreCase(uri.getScheme())) {
			String path = uri.getPath();
			if (path != null) {
				try {
					path = URLDecoder.decode(path, StandardCharsets.UTF_8.toString());
				} catch (Exception e) {
					// Failed to decode path
					return false;
				}
				File file = new File(path);
				return file.exists();
			}
		}
		return false;
	}

	public static boolean isValidUrl(URI uri) {
		try {
			if (uri.getPath().equals(""))
				return false;
			URLDecoder.decode(uri.getPath(), StandardCharsets.UTF_8.toString());
		} catch (Exception e) {
			// Failed to decode path
			return false;
		}
		return true;
	}

	public static byte[] convertToByteArray(ArrayList<Byte> arrayList) {
		byte[] byteArray = new byte[arrayList.size()];

		for (int i = 0; i < arrayList.size(); i++) {
			byteArray[i] = arrayList.get(i);
		}

		return byteArray;
	}
}