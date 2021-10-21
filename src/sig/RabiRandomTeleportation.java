package sig;
import sig.utils.PsapiTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import sig.utils.Module;

public class RabiRandomTeleportation {
	final int[] verticalHeights = { 12, 11, 11, 11, 12, 11, 11, 11, 12, 11, 11, 11, 12, 11, 11, 11, 12, 8 };
	final int PROCESS_PERMISSIONS = WinNT.PROCESS_QUERY_INFORMATION | WinNT.PROCESS_VM_READ | WinNT.PROCESS_VM_WRITE;
	public HANDLE rabiribiProcess = null;
	int rabiRibiPID = -1;
	long rabiRibiMemOffset = 0;
	
	long Sprite1Address = 0;
	long upperLeftRoomArray = 0;
	long entityArrayPtr = 0;
	long ErinaXAddress = 0; 
	long ErinaYAddress = 0;
	long mapAreaXAddress = 0;
	long mapAreaYAddress = 0;
	long bunnyTeleportingAddress = 0;
	long areaAddress = 0;
	long fightingBossAddress = 0;
	
	long MIN_ALIVE_TIME=45000;
	long MAX_ALIVE_TIME=75000;
	long LAST_TELEPORT_TIME=System.currentTimeMillis();
	long NEXT_TELEPORT_TIME=(long)(Math.random()*(MAX_ALIVE_TIME-MIN_ALIVE_TIME))+MIN_ALIVE_TIME+LAST_TELEPORT_TIME;
	
	List<Location> loc = new ArrayList<Location>();
	
	private void CheckRabiRibiClient() {
		List<Integer> pids;
		try {
			pids = PsapiTools.getInstance().enumProcesses();	
			boolean found=false;	
			for (Integer pid : pids) {
				HANDLE process = Kernel32.INSTANCE.OpenProcess(PROCESS_PERMISSIONS, true, pid);
		        List<Module> hModules;
				try {
					hModules = PsapiTools.getInstance().EnumProcessModules(process);
					for(Module m: hModules){
						//System.out.println(m.getFileName()+":"+m.getEntryPoint());
						if (m.getFileName().contains("rabiribi")) {
							found=true;
							rabiRibiMemOffset = Pointer.nativeValue(m.getLpBaseOfDll().getPointer());
							System.out.println("Found an instance of Rabi-Ribi at 0x"+Long.toHexString(rabiRibiMemOffset)+" | File:"+m.getFileName()+","+m.getBaseName());
							rabiRibiPID=pid;
							rabiribiProcess=process;
							break;
						}
			        }
					if (found) {
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (process!=null) {
					Kernel32.INSTANCE.CloseHandle(process);
				}
			}
			if (!found) {
				System.out.println("Rabi-Ribi process lost.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int readIntFromMemory(long offset) {
		Memory mem = new Memory(4);
		Kernel32.INSTANCE.ReadProcessMemory(rabiribiProcess, new Pointer(rabiRibiMemOffset+offset), mem, 4, null);
		return mem.getInt(0);
	}

	public float readFloatFromMemory(long offset) {
		Memory mem = new Memory(4);
		Kernel32.INSTANCE.ReadProcessMemory(rabiribiProcess, new Pointer(rabiRibiMemOffset+offset), mem, 4, null);
		return mem.getFloat(0);
	}
	public byte[] readBytesFromMemory(long offset,int numOfBytes) {
		Memory mem = new Memory(numOfBytes);
		Kernel32.INSTANCE.ReadProcessMemory(rabiribiProcess, new Pointer(rabiRibiMemOffset+offset), mem, numOfBytes, null);
		return mem.getByteArray(0, numOfBytes);
	}
	
	public int readIntFromPointer(long val, long pointer) {
		Memory mem = new Memory(4);
		Kernel32.INSTANCE.ReadProcessMemory(rabiribiProcess, new Pointer(readIntFromMemory(pointer)+val), mem, 4, null);
		return mem.getInt(0);
	}
	
	public void writeIntToMemory(long offset,int value) {
		//Pointer valueptr = new Pointer();
		Memory valueptr = new Memory(8);
		valueptr.setInt(0, value);
		//new Pointer(rabiRibiMemOffset+offset).setMemory((long)0, (long)4, (byte)value);
		Kernel32.INSTANCE.WriteProcessMemory(rabiribiProcess, 
				new Pointer(rabiRibiMemOffset+offset),valueptr,4,null);
		//Kernel32.INSTANCE.ReadProcessMemory(rabiribiProcess, new Pointer(rabiRibiMemOffset+offset), mem, 4, null);
		//return mem.getInt(0);
	}
	
	public void writeShortToMemory(long offset,short value) {
		//Pointer valueptr = new Pointer();
		Memory valueptr = new Memory(4);
		valueptr.setShort((short)0,(short)value);
		//new Pointer(rabiRibiMemOffset+offset).setMemory((long)0, (long)4, (byte)value);
		Kernel32.INSTANCE.WriteProcessMemory(rabiribiProcess, 
				new Pointer(rabiRibiMemOffset+offset),valueptr,2,null);
		//Kernel32.INSTANCE.ReadProcessMemory(rabiribiProcess, new Pointer(rabiRibiMemOffset+offset), mem, 4, null);
		//return mem.getInt(0);
	}
	
	public void writeFloatToMemory(long offset,float value) {
		writeIntToMemory(offset,Float.floatToIntBits(value));
	}
	
	public float readFloatFromPointer(long offset, long pointer) {
		Memory mem = new Memory(4);
		Kernel32.INSTANCE.ReadProcessMemory(rabiribiProcess, new Pointer(readIntFromMemory(pointer)+offset), mem, 4, null);
		return mem.getFloat(0);
	}
	
	public void updateEventValue(short value, int roomX, int roomY, int x, int y) {
		int finalIndex = y+x*200;
		int verticalHeight = 0;
		for (int i=0;i<roomY;i++) {
			verticalHeight+=verticalHeights[i];
		}
		finalIndex+=verticalHeight;
		finalIndex+=roomX*4000;
		//System.out.println(finalIndex);
		long targetAddress = upperLeftRoomArray+finalIndex*2;
		//System.out.println(targetAddress);
		writeShortToMemory(targetAddress,value);
	}
	
	public static String[] readFromFile(String filename) {
		File file = new File(filename);
		//System.out.println(file.getAbsolutePath());
		List<String> contents= new ArrayList<String>();
		if (file.exists()) {
			try(
					FileReader fw = new FileReader(filename);
				    BufferedReader bw = new BufferedReader(fw);)
				{
					String readline = bw.readLine();
					do {
						if (readline!=null) {
							//System.out.println(readline);
							contents.add(readline);
							readline = bw.readLine();
						}} while (readline!=null);
					fw.close();
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return contents.toArray(new String[contents.size()]);
	}
	
	RabiRandomTeleportation() {
		for (int i=0;i<9;i++) {
			String[] data = readFromFile("map/area"+i+".txt");
			for (String s : data) {
				if (s.length()>0) {
					String[] split = s.split(Pattern.quote(","));
					if (split.length==3) {
						if (Integer.parseInt(split[2])!=-1) {
							loc.add(new Location(i,Integer.parseInt(split[0]),Integer.parseInt(split[1])));
						}
					}
				}
			}
		}
		//System.out.println(loc.size()+" locations loaded!");
		
		CheckRabiRibiClient();
		//System.out.println(Arrays.toString(readBytesFromMemory(0xE47290,8)));
		//System.out.println(Sprite1Address);
		Sprite1Address = 0xE47290;
		upperLeftRoomArray = Sprite1Address-0x93250;
		mapAreaXAddress = 0xFFE9DC;
		mapAreaYAddress = 0xD7999C;
		//Room X:17, Room Y:13
		entityArrayPtr = 0x96DA3C;
		bunnyTeleportingAddress = 0xA73054;
		fightingBossAddress = 0xA72E08;
		areaAddress = 0xA600AC;
		/*System.out.println("Erina X:"+readFloatFromPointer(0xC,entityArrayPtr));
		System.out.println("Erina Y:"+readFloatFromPointer(0xC+0x4,entityArrayPtr));
		System.out.println("Map: "+readIntFromMemory(mapAreaXAddress)+","+readIntFromMemory(mapAreaYAddress));
		long entityArrayPtrOffset = readIntFromMemory(0x96DA3C)-rabiRibiMemOffset;
		writeFloatToMemory(entityArrayPtrOffset+0xC,9600);
		writeFloatToMemory(entityArrayPtrOffset+0xC+0x4,9240);*/
		//updateEventValue((short)166,16,12,15,7);
		//20x11
		//TeleportBunnyToRandomLocation();
		while (true) {
			System.out.println(System.currentTimeMillis()+"/"+NEXT_TELEPORT_TIME);
			if (System.currentTimeMillis()>NEXT_TELEPORT_TIME&&readIntFromMemory(fightingBossAddress)!=1) {
				TeleportBunnyToRandomLocation();
				LAST_TELEPORT_TIME=System.currentTimeMillis();
				NEXT_TELEPORT_TIME=(long)(Math.random()*(MAX_ALIVE_TIME-MIN_ALIVE_TIME))+MIN_ALIVE_TIME+LAST_TELEPORT_TIME;
				//System.out.println(NEXT_TELEPORT_TIME);
				 
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void TeleportBunnyToRandomLocation() {
		Location chosenLoc = loc.get((int)(Math.random()*loc.size()));
		long entityArrayPtrOffset = readIntFromMemory(0x96DA3C)-rabiRibiMemOffset;
		for (int x=0;x<20;x++) {
			for (int y=0;y<11;y++) {
				updateEventValue((short)(161+chosenLoc.area),readIntFromMemory(mapAreaXAddress),readIntFromMemory(mapAreaYAddress),x,y);
			}
		}
		int tries=0;
		do {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tries++;
		} while (tries<200&&readIntFromMemory(areaAddress)!=chosenLoc.area);
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (tries<200) {
			System.out.println(chosenLoc);
			
			//1280 width per room, 800 height per room.
			writeFloatToMemory(entityArrayPtrOffset+0xC,1280*chosenLoc.roomX+640);
			writeFloatToMemory(entityArrayPtrOffset+0xC+0x4,720*chosenLoc.roomY+600);
		}
	}

	public static void main(String[] args) {
		RabiRandomTeleportation app = new RabiRandomTeleportation();
	}
}
