package sig;
import sig.utils.PsapiTools;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import sig.modules.RabiRibi.MemoryOffset;
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
	
	RabiRandomTeleportation() {
		CheckRabiRibiClient();
		//System.out.println(Arrays.toString(readBytesFromMemory(0xE47290,8)));
		//System.out.println(Sprite1Address);
		Sprite1Address = 0xE47290;
		upperLeftRoomArray = Sprite1Address-0x93250;
		//Room X:17, Room Y:13
		entityArrayPtr = 0x96DA3C;
		long entityArrayPtrOffset = readIntFromMemory(0x96DA3C)-rabiRibiMemOffset;
		System.out.println("Erina X:"+readFloatFromPointer(0xC,entityArrayPtr));
		System.out.println("Erina Y:"+readFloatFromPointer(0xC+0x4,entityArrayPtr));
		writeFloatToMemory(entityArrayPtrOffset+0xC+0x4,5000);
		//updateEventValue((short)166,16,12,15,7);
		
	}
	
	public static void main(String[] args) {
		RabiRandomTeleportation app = new RabiRandomTeleportation();
	}
}
