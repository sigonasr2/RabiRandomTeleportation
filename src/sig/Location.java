package sig;

public class Location {
	int area,roomX,roomY;
	Location(int area,int x,int y) {
		this.area=area;
		this.roomX=x;
		this.roomY=y;
	}
	@Override
	public String toString() {
		return "Location [area=" + area + ", roomX=" + roomX + ", roomY=" + roomY + "]";
	}
}
