package net.sourceforge.tess4j.example;
//package net.sourceforge.tess4j.example;
//
//import java.io.File;
//
//import net.sourceforge.tess4j.Tesseract;
//import net.sourceforge.tess4j.TesseractException;
//
//public class TesseractExample {
//
//	public static void main(String[] args) {
//		File imageFile = new File("c:/test5.png");
//		Tesseract instance = Tesseract.getInstance(); // JNA Interface Mapping
//		// Tesseract1 instance = new Tesseract1(); // JNA Direct Mapping
//
//		try {
//			String result = instance.doOCR(imageFile);
//			System.out.println(result);
//		}
//		catch (TesseractException e) {
//			System.err.println(e.getMessage());
//		}
//	}
// }