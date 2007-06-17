package org.newdawn.slick.svg.inkscape;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.svg.Diagram;
import org.newdawn.slick.svg.Figure;
import org.newdawn.slick.svg.Loader;
import org.newdawn.slick.svg.NonGeometricData;
import org.newdawn.slick.svg.ParsingException;
import org.w3c.dom.Element;

/**
 * A processor for the <polygon> and <path> elements marked as not an arc.
 *
 * @author kevin
 */
public class PolygonProcessor implements ElementProcessor {

	/**
	 * Process the points in a polygon definition
	 * 
	 * @param poly The polygon being built
	 * @param element The XML element being read
	 * @param tokens The tokens representing the path
	 * @return The number of points found
	 * @throws ParsingException Indicates an invalid token in the path
	 */
	private static int processPoly(Polygon poly, Element element, StringTokenizer tokens) throws ParsingException {
		int count = 0;
		
		ArrayList pts = new ArrayList();
		boolean moved = false;
		
		while (tokens.hasMoreTokens()) {
			String nextToken = tokens.nextToken();
			if (nextToken.equals("L")) {
				continue;
			}
			if (nextToken.equals("z")) {
				break;
			}
			if (nextToken.equals("M")) {
				if (!moved) {
					moved = true;
					tokens.nextToken();
					tokens.nextToken();
					continue;
				}
				
				return 0;
			}
			if (nextToken.equals("C")) {
				return 0;
			}
			
			String tokenX = nextToken;
			String tokenY = tokens.nextToken();
			
			try {
				float x = Float.parseFloat(tokenX);
				float y = Float.parseFloat(tokenY);
				
				poly.addPoint(x,y);
				count++;
			} catch (NumberFormatException e) {
				throw new ParsingException(element.getAttribute("id"), "Invalid token in points list", e);
			}
		}
		
		return count;
	}


	/**
	 * @see org.newdawn.slick.svg.inkscape.ElementProcessor#process(org.newdawn.slick.svg.Loader, org.w3c.dom.Element, org.newdawn.slick.svg.Diagram)
	 */
	public void process(Loader loader, Element element, Diagram diagram) throws ParsingException {
		Transform transform = Util.getTransform(element);
		String points = element.getAttribute("points");
		if (element.getNodeName().equals("path")) {
			points = element.getAttribute("d");
		}
		
		StringTokenizer tokens = new StringTokenizer(points, ", ");
		Polygon poly = new Polygon();
		int count = processPoly(poly, element, tokens);
		if (count > 3) {
			Shape shape = poly.transform(transform);
			
			NonGeometricData data = Util.getNonGeometricData(element);
			
			diagram.addFigure(new Figure(Figure.POLYGON, shape, data, transform));
		}
	}

	/**
	 * @see org.newdawn.slick.svg.inkscape.ElementProcessor#handles(org.w3c.dom.Element)
	 */
	public boolean handles(Element element) {
		if (element.getNodeName().equals("polygon")) {
			return true;
		}
		
		if (element.getNodeName().equals("path")) {
			if (!element.getAttributeNS(Util.SODIPODI, "type").equals("arc")) {
				return true;
			}
		}
		
		return false;
	}
}
