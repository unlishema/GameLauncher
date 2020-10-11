

public class Version {
	
	public static final Version THIS = new Version(0, 0, 5);

	public static boolean debug = false;
	public static boolean debugDetail = false;
	
	public final int major;
	public final int minor;
	public final int patch;

	public Version(final String version) {
		final String[] split = version.split("\\.");
		if(split.length >= 1) this.major = Integer.parseInt(split[0]);
		else this.major = 0;
		if(split.length >= 2) this.minor = Integer.parseInt(split[1]);
		else this.minor = 0;
		if(split.length >= 3) this.patch = Integer.parseInt(split[2]);
		else this.patch = 0;
	}

	public Version(final int major, final int minor, final int patch) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
	}
	
	public boolean greaterThan(final Object object) {
		if(object instanceof Version) {
			final Version v = (Version) object;
			if(this.major > v.major) return true;
			if(this.majorEquals(object) && this.minor > v.minor) return true;
			if(this.majorEquals(object) && this.minorEquals(object) && this.patch > v.patch) return true;
		}
		return false;
	}
	
	public boolean lessThan(final Object object) {
		if(object instanceof Version) {
			final Version v = (Version) object;
			if(this.major < v.major) return true;
			if(this.majorEquals(object) && this.minor < v.minor) return true;
			if(this.majorEquals(object) && this.minorEquals(object) && this.patch < v.patch) return true;
		}
		return false;
	}
	
	public boolean majorEquals(final Object object) {
		if(object instanceof Version) {
			final Version v = (Version) object;
			if(this.major == v.major) return true;
		}
		return false;
	}
	
	public boolean minorEquals(final Object object) {
		if(object instanceof Version) {
			final Version v = (Version) object;
			if(this.minor == v.minor) return true;
		}
		return false;
	}
	
	public boolean patchEquals(final Object object) {
		if(object instanceof Version) {
			final Version v = (Version) object;
			if(this.patch == v.patch) return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(final Object object) {
		return this.majorEquals(object) && this.minorEquals(object) && this.patchEquals(object);
	}
	
	@Override
	public String toString() {
		return this.major + "." + this.minor + "." + this.patch;
	}
}