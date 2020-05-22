package fr.jamailun.stds.server.commands;

public interface CommandExecutor {
	void executeCommand(String label, String[] args);
	void startThread();
}