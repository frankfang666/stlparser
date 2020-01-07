import java.lang.*;
import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
public class STLParser {
	static Parser p = new Parser("Impeller.stl");
	public static void output(){
		String str = p.r.getName();
		int len = str.length();
		if(str.substring(len-3, len).equals("stl") || str.substring(len-3, len).equals("STL")){
			try{
				p.BinaryOut();
			}
			catch(Exception e){
				e.printStackTrace();
				System.out.println("Exception occurs!");
			}
		}
		else{
			p.TextOut();
		}
	}
	public static void main(String args[]){
		output();
	}
}

class Parser{
	Reading r;
	public Parser(String filenames){
		this.r = new Reading(filenames);
	}
	public void TextOut(){
		r.OpenFile();
		Solid s = r.ReadSTLTextFile();
		System.out.printf("The surface area of the solid is %f.\n", s.calSurface());
		TreeSet<Triangle> t = s.sorting();
		s.show(t);
	}
	public void BinaryOut() throws Exception{
		r.OpenFile(r.getName());
		Solid s = r.ReadSTLBinaryFile();
		System.out.printf("The surface area of the solid is %f.\n", s.calSurface());
		TreeSet<Triangle> t = s.sorting();
		s.show(t);
	}
}

class Reading{
	private Scanner s;
	private String name;
	private BufferedInputStream bis;
	public Reading(String filenames){
		this.name = filenames;
	}
	public String getName(){
		return this.name;
	}
	public void OpenFile(){
		try{
			String m = this.name;
			s = new Scanner(new File(m));
		}
		catch(Exception e){
			System.out.println("File cannot be opened.");
		}
	}
	public void OpenFile(String st){
		try{
			st = this.name;
			InputStream in1 = new FileInputStream(st);
			bis = new BufferedInputStream(in1);
		}
		catch(Exception e){
			System.out.println("File cannot be opened.");
		}
	}
	public static boolean isNumeric(String str) { 
		try {
			Double.parseDouble(str);  
			return true;
		}
		catch(NumberFormatException e){
			return false;
		}  
	}
	public byte[] reverse(byte[] b){
		byte[] b0 = new byte[b.length];
		System.arraycopy(b, 0, b0, 0, b.length);
		for (int i=0; i<b.length; i++){
			b0[i] = b[b.length -1 -i];
		}
		return b0;
	}
	public Solid ReadSTLTextFile(){
		double normals[] = new double[3];
		double v[]  = new double[3];
		boolean is_ver = false, is_norm = false;
		int i = 0, j = 0;
		int f_count = 0;
		Triangle t = new Triangle();
		Solid so = new Solid();
		while(s.hasNext()){
			String n = s.next();
			if (is_norm){
				if (isNumeric(n)){
					normals[j++] = Double.valueOf(n);
				}
				else{
					Normal nom = new Normal(normals[0], normals[1], normals[2]);
					t.setNorm(nom);
					j = 0;
					is_norm = false;
				}
			}
			if (is_ver){
				if (isNumeric(n)){
					v[i++] = Double.valueOf(n);
				}
				else if(n.equals("vertex") || n.equals("endloop")){
					is_ver = false;
					Vertex ver = new Vertex(v[0], v[1], v[2]);
					t.vertices.add(ver);
				}
			}
			if (n.equals("facet")){
				if (f_count>0){
					so.triangles.add(t);
				}
				t = new Triangle();
				f_count++;
			}
			else if (n.equals("vertex")){
				i = 0;
				is_ver = true;
			}
			else if (n.equals("normal")){
				is_norm = true;
			}
		}
		so.triangles.add(t);
		return so;
	}
	public Solid ReadSTLBinaryFile() throws Exception{
		int count = 0;
		int f = 0;
		Solid so = new Solid();
		int len = bis.available();
		byte [] b = new byte[len];
		bis.read(b, 0, len);
		byte [] tris = new byte[len-84];
		System.arraycopy(b, 84, tris, 0, len-84);
		byte [] b2 = new byte[4];
		byte [] tri = new byte[50];
		double [] nums = new double[12];
		while(count<tris.length){
			System.arraycopy(tris, count, tri, 0, 50);
			Triangle t = new Triangle();
			for(int m=0; m<tri.length-2; m+=4){
				System.arraycopy(tri, m, b2, 0, 4);
				ByteBuffer buffer = ByteBuffer.wrap(reverse(b2));
				nums[f++] = (double)buffer.getFloat();
			}
			Normal norm = new Normal(nums[0], nums[1], nums[2]);
			t.setNorm(norm);
			for (int l=3; l<nums.length; l+=3){
				Vertex ver = new Vertex(nums[l], nums[l+1], nums[l+2]);
				t.vertices.add(ver);
			}
			so.triangles.add(t);
			f = 0;
			count += 50;
		}
		return so;
	}
}

class Normal{
	private double x;
	private double y;
	private double z;
	public Normal(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public double getXnor(){
		return this.x;
	}
	public double getYnor(){
		return this.y;
	}
	public double getZnor(){
		return this.z;
	}
}

class Vertex{
	private double x;
	private double y;
	private double z;
	public Vertex(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public double getXver(){
		return this.x;
	}
	public double getYver(){
		return this.y;
	}
	public double getZver(){
		return this.z;
	}
}

class Triangle{
	private Normal nor;
	public ArrayList<Vertex> vertices = new ArrayList<>();
	public void setNorm(Normal n){
		this.nor = n;
	}
	public void showNorm(){
		System.out.printf("with the normal of the triangle being %f %f %f\n", 
		nor.getXnor(), nor.getYnor(), nor.getZnor());
	}
	double calCoord(Vertex v1, Vertex v2){
		return Math.sqrt(Math.pow((v1.getXver()-v2.getXver()), 2) + 
		Math.pow((v1.getYver()-v2.getYver()), 2) + 
		Math.pow((v1.getZver()-v2.getZver()), 2));
	}
	double calFace(){
		double l1, l2, l3;
		l1 = this.calCoord(vertices.get(0), vertices.get(1));
		l2 = this.calCoord(vertices.get(1), vertices.get(2));
		l3 = this.calCoord(vertices.get(0), vertices.get(2));
		double p = (l1 + l2 + l3) / 2.0;
		return Math.sqrt(p*(p-l1)*(p-l2)*(p-l3));
	}
}

class Solid{
	public ArrayList<Triangle> triangles = new ArrayList<>();
	double calSurface(){
		double area = 0.0;
		for (int i=0; i<triangles.size(); i++){
			area += triangles.get(i).calFace();
		}
		return area;
	}
	public TreeSet<Triangle> sorting(){
		TreeSet<Triangle> tree = new TreeSet<>(new compare());
		for (int j=0; j<triangles.size(); j++){
			tree.add(triangles.get(j));
		}
		return tree;
	}
	public void show(TreeSet<Triangle> tree){
		System.out.println("The area of each triangle is sorted and is shown as following: ");
		Iterator<Triangle> it = tree.iterator();
		while (it.hasNext()){
			Triangle o  = (Triangle)it.next();
			System.out.print(o.calFace() + " ");
			o.showNorm();
		}
	}
}

class compare implements Comparator<Triangle>{
	public int compare(Triangle t1, Triangle t2){
		if (t1.calFace() < t2.calFace()) return -1;
		else if (t1.calFace() > t2.calFace()) return 1;
		else return 0;
	}
}