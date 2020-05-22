package fr.jamailun.stds.common;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ClientInfo implements Serializable {
	private static final long serialVersionUID = 400L;

	private final UUID uuid;
	private final String name;

	public ClientInfo(String name) {
		this.name = name;
		uuid = UUID.randomUUID();
	}

	private ClientInfo(String name, UUID uuid) {
		this.name = name;
		this.uuid = uuid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClientInfo that = (ClientInfo) o;
		return uuid.equals(that.uuid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uuid, name);
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public ClientInfo rename(String newName) {
		return new ClientInfo(newName, uuid);
	}

	@Override
	public String toString() {
		return "ClientInfo[" +
				"uuid=" + getUuid() +
				", name='" + getName() + '\'' +
				']';
	}
}