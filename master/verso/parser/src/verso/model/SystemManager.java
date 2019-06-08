package verso.model;

import java.util.Hashtable;
import java.util.Set;

public class SystemManager {

	private static Hashtable<String, SystemDef> systemTable = new Hashtable<String, SystemDef>();

	public static void addSystem(SystemDef sys) {
		systemTable.put(sys.getName(), sys);
	}

	public static SystemDef getSystem(String sysName) {
		return systemTable.get(sysName);
	}

	public static String getProjectsName() {
		String toReturn = "";
		for (String sys : systemTable.keySet()) {
			toReturn += sys + ",";
		}
		return toReturn;
	}

	public static Set<String> getProjects() {
		return systemTable.keySet();
	}

	public static void removeProject(String projName) {
		systemTable.remove(projName);
	}

}
