package fr.jamailun.stds.common;

import java.util.Objects;

public class Version {
	private final String versionString;
	public Version(String versionString) {
		this.versionString = versionString;
	}
	public boolean isSame(Version version) {
		return this.versionString.equals(version.versionString);
	}

	@Override
	public String toString() {
		return "Version[versionString=" + versionString + "]";
	}

	public String getVersionString() {
		return versionString;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		return isSame(((Version)o));
	}

	public boolean isAfter(Version version) {
		String[] v1 = this.versionString.split("\\.");
		String[] v2 = version.versionString.split("\\.");
		for(int i = 0; i < Math.min(v1.length, v2.length); i++) {
			int n1, n2 = -1;
			try {
				n1 = Integer.parseInt(v1[i]);
				n2 = Integer.parseInt(v2[i]);
			} catch (NumberFormatException e) {
				System.err.println("Bad version format ! ("+v1[i]+"/"+v2[i]+").");
				return false;
			}
			if(n1 < 0 || n2 < 0) {
				System.err.println("Bad version format !"+v1[i]+"/"+v2[i]+").");
				return false;
			}
			if(n1 > n2)
				return true;
		}
		return v1.length > v2.length;
	}

	@Override
	public int hashCode() {
		return Objects.hash(versionString);
	}
}