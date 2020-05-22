package fr.jamailun.stds.server.commands;

import java.util.Arrays;
import java.util.List;

public class Command {

	private final String label, description;
	private final CommandTreatment treatment;
	private final List<String> aliases;
	public Command(String label, CommandTreatment treatment, String description, String... aliases) {
		this.label = label;
		this.treatment = treatment;
		this.description = description;
		this.aliases = Arrays.asList(aliases);
	}

	public String getLabel() {
		return label;
	}

	public CommandTreatment getTreatment() {
		return treatment;
	}

	public String[] getAliases() {
		String[] strings = new String[aliases.size()];
		for(int i = 0; i < strings.length; i++)
			strings[i] = aliases.get(i);
		return strings;
	}

	public boolean matches(String label) {
		return label.equals(this.label) || aliases.contains(label);
	}

	public String getDescription() {
		return description;
	}

	public interface CommandTreatment {
		void execute(String[] args);
	}
}