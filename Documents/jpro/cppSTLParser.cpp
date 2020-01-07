#include <iostream>
#include <fstream>
#include <string>
#include <cstring>
#include <list>
#include <cmath>
#include <algorithm>
#include <vector>
#include "cppSTLParser.h"
using namespace std;

bool Read_STL_text::is_numeric(string s){
	try{
		stod(s);
	}
	catch(...){
		return false;
	}
	return true;
}

void Triangle::show_norm(){
		cout << "with the normal of the triangle being " << nor.get_xnor() << " "
		<< nor.get_ynor() << " " << nor.get_znor() << endl;
}

double Triangle::cal_coord(Vertex v1, Vertex v2){
	return sqrt(pow((v1.get_xver()-v2.get_xver()), 2) + 
	pow((v1.get_yver()-v2.get_yver()), 2) + 
	pow((v1.get_zver()-v2.get_zver()), 2));
}

double Triangle::cal_face(){
	double l1, l2, l3;
	l1 = this->cal_coord(vertices[0], vertices[1]);
	l2 = this->cal_coord(vertices[1], vertices[2]);
	l3 = this->cal_coord(vertices[0], vertices[2]);
	double p = (l1 + l2 + l3) / 2.0;
	return sqrt(p*(p-l1)*(p-l2)*(p-l3));
}
void Triangle::show_area(){
	cout << "The area of the triangle is " << this->cal_face() << " ";
}

Solid Read_STL_text::read_file(){
	string s;
	double normals[3];
	double v[3];
	bool is_ver = false, is_norm = false;
	int i = 0, j = 0, l = 0;
	int f_count = 0;
	Triangle t;
	Solid so = Solid();
	while(ifs >> s){
		if (is_norm){
			if (is_numeric(s)){
				normals[j++] = stod(s);
			}
			else{
				Normal nom = Normal(normals[0], normals[1], normals[2]);
				t = Triangle(nom);
				j = 0;
				is_norm = false;
			}
		}
		if (is_ver){
			if (is_numeric(s)){
				v[i++] = stod(s);
			}
			else if(s == "vertex" || s == "endloop"){
				is_ver = false;
				Vertex ver = Vertex(v[0], v[1], v[2]);
				t.vertices[l++] = ver;
			}
		}
		if (s == "facet"){
			if (f_count>0){
				so.triangles.push_back(t);
			}
			l = 0;
			t = Triangle();
			f_count++;
		}
		else if (s == "vertex"){
			i = 0;
			is_ver = true;
		}
		else if (s == "normal"){
			is_norm = true;
		}
	}
	so.triangles.push_back(t);
	return so;
}


Vertex::Vertex(double x, double y, double z){
	this->x = x;
	this->y = y;
	this->z = z;
}
double Vertex::get_xver(){
	return this->x;
}
double Vertex::get_yver(){
	return this->y;
}
double Vertex::get_zver(){
	return this->z;
}

Normal::Normal(double x, double y, double z){
	this->x = x;
	this->y = y;
	this->z = z;
}

double Normal::get_xnor(){
	return this->x;
}
double Normal::get_ynor(){
	return this->y;
}
double Normal::get_znor(){
	return this->z;
}

double Solid::cal_surface(){
	double area = 0.0;
	list <Triangle>::iterator it; 
    for(it = triangles.begin(); it != triangles.end(); it++){
        area += it->cal_face();
	}
	return area;
}

void Solid::traverse(){
	list <Triangle>::iterator it; 
	for(it = triangles.begin(); it != triangles.end(); it++){
		it->show_area();
		it->show_norm();
	}
}

struct comparator{
	bool operator()(Triangle a, Triangle b){
		return a.cal_face() < b.cal_face();
	}
};

char* Read_STL_Bin::fills(vector<char> srcArray, char subArray[], int len, int start){
    for (int i = 0; i < len; i++){
        subArray[i] = srcArray[start++];
    }
	return subArray;
}

char* Read_STL_Bin::fills(char srcArray[], char subArray[], int len, int start){
    for (int i = 0; i < len; i++){
        subArray[i] = srcArray[start++];
    }
	return subArray;
}

float Read_STL_Bin::btof(char b3, char b2, char b1, char b0){
	float f;
	char b[] = {b3, b2, b1, b0};
	memcpy(&f, &b, sizeof(f));
	return f;
}

Solid Read_STL_Bin::read_file(){
	Solid so;
	Triangle tri;
	vector<char> buffer;
	double nums[12];
	char one[4];
	char triangle[48];
	int c, d;
	istream is(&fb);
	while (is){
		buffer.push_back(char(is.get()));
	}
	for (int j = 84; j < int(buffer.size())-2 ; j+=50){
		fills(buffer, triangle, 48, j);
		
		/* fill in 12 doubles in the array for one triangle */
		for (int k = 0; k < 48; k+=4){
			c = k / 4;
			fills(triangle, one, 4, k);
			nums[c] = (double)btof(one[0], one[1], one[2], one[3]);
		}
		
		/* set the normal of the triangle */
		Normal nor = Normal(nums[0], nums[1], nums[2]);
		tri = Triangle(nor);

		/* obtain the remaining elements in the triangle as vertices*/
		for (int l = 3; l < 12; l+=3){
			d = l / 3 - 1;
			Vertex ver = Vertex(nums[l], nums[l+1], nums[l+2]);
			tri.vertices[d] = ver;
		}
		so.triangles.push_back(tri);
	}
	fb.close();
	return so;
}

void determine_process::determine(){
	Solid s;
	if (f_name.substr(f_name.length()-3, 3) == "stl" || f_name.substr(f_name.length()-3, 3) == "STL"){
		Read_STL_Bin r = Read_STL_Bin(f_name);
		r.open_file();
		s = r.read_file();
	}
	else{
		Read_STL_text r = Read_STL_text(f_name);
		r.open_file();
		s = r.read_file();
	}
	cout << "The surface area of the solid is " << s.cal_surface() << "." << endl;
	s.triangles.sort(comparator());
	s.traverse();
}

int main(int argc, char *argv[]){
	determine_process d = determine_process("Impeller.stl");
	d.determine();
	return 0;
}