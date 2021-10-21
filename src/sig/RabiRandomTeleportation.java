package sig;
import sig.utils.PsapiTools;

import java.util.List;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import sig.utils.Module;

public class RabiRandomTeleportation {
	final int PROCESS_PERMISSIONS = WinNT.PROCESS_QUERY_INFORMATION | WinNT.PROCESS_VM_READ | WinNT.PROCESS_VM_WRITE;
	public HANDLE rabiribiProcess = null;
	int rabiRibiPID = -1;
	
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
							long rabiRibiMemOffset = Pointer.nativeValue(m.getLpBaseOfDll().getPointer());
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
	
	public static void main(String[] args) {
		RabiRandomTeleportation app = new RabiRandomTeleportation();
		app.CheckRabiRibiClient();
	}
}
