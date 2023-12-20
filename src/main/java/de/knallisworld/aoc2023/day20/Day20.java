package de.knallisworld.aoc2023.day20;

import de.knallisworld.aoc2023.support.math.Utils;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static de.knallisworld.aoc2023.support.cli.Commons.printHeader;
import static de.knallisworld.aoc2023.support.cli.Commons.printSolution;
import static de.knallisworld.aoc2023.support.puzzle.InputReader.readInputLines;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

@Log4j2
public class Day20 {

	public static void main(String[] args) {
		printHeader(20);
		printSolution(1, () -> part1(parseInput(readInputLines(20, "part1"))));
		printSolution(2, () -> part2(parseInput(readInputLines(20, "part1"))));
	}

	enum Type {
		BROADCASTER,
		FLIP_FLOP,
		CONJUNCTION,
		OUTPUT
	}

	enum Pulse {
		CONFIGURE,
		HIGH,
		LOW
	}

	interface Environment {

		void configure();

		void initiate();

		void send(Pulse pulse, Module origin);

		List<Module> getConnectedFrom(Module module);

		List<Module> getConnectedTo(Module module);

		default List<Module> getConnectedToByName(String name) {
			return getConnectedTo(getModule(name));
		}

		Map<Pulse, Long> getPulseStatistics();

		void addListener(String name, BiConsumer<Module, Pulse> listener);

		Module getModule(String name);
	}

	interface Module {

		String getName();

		Type getType();

		void receive(Pulse pulse, Module origin, Environment env);

	}

	static class Broadcaster implements Module {

		private final String name;

		public Broadcaster(final String name) {
			this.name = name;
		}

		@Override
		public Type getType() {
			return Type.BROADCASTER;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void receive(Pulse pulse, Module origin, Environment env) {
			switch (pulse) {
				case LOW, HIGH -> env.send(pulse, this);
			}
		}

	}

	static class OutputModule implements Module {

		private final String name;

		public OutputModule(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Type getType() {
			return Type.OUTPUT;
		}

		@Override
		public void receive(Pulse pulse, Module origin, Environment env) {
			// empty
			// System.out.println("Got pulse: " + pulse);
		}

	}

	static class FlipFlopModule implements Module {

		private final String name;

		private boolean state;

		FlipFlopModule(final String name) {
			this.name = name;
			state = false;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Type getType() {
			return Type.FLIP_FLOP;
		}

		@Override
		public void receive(final Pulse pulse, final Module origin, final Environment env) {
			switch (pulse) {
				case LOW -> {
					final var signal = state
							? Pulse.LOW
							: Pulse.HIGH;
					state = !state;
					env.send(signal, this);
				}
			}
		}
	}

	static class ConjunctionModule implements Module {

		private final String name;

		private final Map<String, Pulse> mem;

		public ConjunctionModule(String name) {
			this.name = name;
			this.mem = new HashMap<>();
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Type getType() {
			return Type.CONJUNCTION;
		}

		@Override
		public void receive(final Pulse pulse, final Module origin, final Environment env) {
			switch (pulse) {
				case CONFIGURE -> {
					mem.clear();
					env.getConnectedTo(this).forEach(module -> {
						mem.put(module.getName(), Pulse.LOW);
					});
				}
				case LOW, HIGH -> {
					mem.put(origin.getName(), pulse);
					if (Set.copyOf(mem.values()).equals(Set.of(Pulse.HIGH))) {
						env.send(Pulse.LOW, this);
					} else {
						env.send(Pulse.HIGH, this);
					}
				}
			}
		}
	}

	record Connection(String src, String dst) {
	}

	static class DefaultEnvironment implements Environment {

		record Message(Module origin, Pulse pulse, Module destination) {
		}

		private final Map<String, Module> modules;
		private final Collection<Connection> connections;
		private final Queue<Message> queue;

		private final Map<Pulse, Long> pulseStats;
		private final Map<String, List<Consumer<Pulse>>> listeners;

		public DefaultEnvironment(final Map<String, Module> modules,
								  final Collection<Connection> connections) {
			this.modules = Map.copyOf(modules);
			this.connections = Set.copyOf(connections);
			this.queue = new LinkedList<>();
			this.pulseStats = new EnumMap<>(Pulse.class);
			Arrays.stream(Pulse.values()).forEach(p -> pulseStats.put(p, 0L));
			this.listeners = new HashMap<>();
		}

		void pushMessage(final Message message) {
			log.debug(() -> "%s -%s-> %s".formatted(
					message.origin.getName(),
					message.pulse,
					message.destination.getName()
			));
			pulseStats.put(message.pulse, pulseStats.get(message.pulse) + 1);
			queue.add(message);
		}

		private void processQueue() {
			while (!queue.isEmpty()) {
				final var message = queue.poll();
				message.destination.receive(message.pulse, message.origin, this);
				listeners.getOrDefault(message.destination.getName(), List.of())
						 .forEach(listener -> listener.accept(message.pulse));
			}
		}

		@Override
		public void configure() {
			modules.values().forEach(module -> pushMessage(new Message(module, Pulse.CONFIGURE, module)));
			processQueue();
		}

		@Override
		public void initiate() {
			final var module = requireNonNull(modules.get("broadcaster"), "broadcaster module missing");
			pushMessage(new Message(module, Pulse.LOW, module));
			processQueue();
		}

		@Override
		public void send(final Pulse pulse, final Module origin) {
			getConnectedFrom(origin)
					.forEach(module -> pushMessage(new Message(origin, pulse, module)));
			processQueue();
		}

		@Override
		public List<Module> getConnectedFrom(final Module module) {
			return connections
					.stream()
					.filter(e -> e.src().equals(module.getName()))
					.map(e -> requireNonNull(modules.get(e.dst()), "invalid module name: " + e.dst()))
					.toList();
		}

		@Override
		public List<Module> getConnectedTo(final Module module) {
			return connections
					.stream()
					.filter(e -> e.dst().equals(module.getName()))
					.map(e -> requireNonNull(modules.get(e.src()), "invalid module name"))
					.toList();
		}

		@Override
		public Map<Pulse, Long> getPulseStatistics() {
			return Map.copyOf(pulseStats);
		}

		@Override
		public void addListener(final String name, final BiConsumer<Module, Pulse> listener) {
			listeners.computeIfAbsent(name, _ -> new ArrayList<>())
					 .add(pulse -> listener.accept(modules.get(name), pulse));
		}

		@Override
		public Module getModule(String name) {
			return requireNonNull(modules.get(name), "invalid module name");
		}
	}

	static Environment parseInput(final List<String> lines) {
		final var modules = new HashMap<String, Type>();
		final var connections = new ArrayList<Connection>();

		lines.forEach(line -> {
			final var source = line.split(" -> ")[0].strip();
			final var sourceName = switch (source.charAt(0)) {
				case 'b' -> {
					modules.put("broadcaster", Type.BROADCASTER);
					yield "broadcaster";
				}
				case '%' -> {
					final var n = source.substring(1);
					modules.put(n, Type.FLIP_FLOP);
					yield n;
				}
				case '&' -> {
					final var n = source.substring(1);
					modules.put(n, Type.CONJUNCTION);
					yield n;
				}
				default -> throw new IllegalStateException("invalid input");
			};
			Arrays.stream(line.split(" -> ")[1].strip().split(","))
				  .map(String::strip)
				  .forEach(destinationName -> connections.add(new Connection(sourceName, destinationName)));
		});
		// register untyped
		connections.stream()
				   .map(Connection::dst)
				   .filter(not(modules::containsKey))
				   .forEach(name -> modules.put(name, Type.OUTPUT));

		return new DefaultEnvironment(
				modules.entrySet()
					   .stream()
					   .map(e -> switch (requireNonNull(e.getValue())) {
						   case BROADCASTER -> new Broadcaster(e.getKey());
						   case FLIP_FLOP -> new FlipFlopModule(e.getKey());
						   case CONJUNCTION -> new ConjunctionModule(e.getKey());
						   case OUTPUT -> new OutputModule(e.getKey());
					   })
					   .collect(toMap(Module::getName, identity())),
				connections
		);
	}

	static String part1(final Environment env) {
		env.configure();
		IntStream.range(0, 1000).forEach(_ -> env.initiate());
		final var stats = env.getPulseStatistics();
		return "result = %d".formatted(stats.get(Pulse.LOW) * stats.get(Pulse.HIGH));
	}

	static String part2(final Environment env) {
		env.configure();

		final var counter = new AtomicLong(0L);
		final var min = new HashMap<String, Long>();
		final BiConsumer<Module, Pulse> listener = (m, pulse) -> {
			if (pulse == Pulse.LOW) {
				if (min.get(m.getName()) == 0L) {
					min.put(m.getName(), counter.get());
					log.trace(() -> "FOUND @" + min);
				}
			}
		};

		//env.addListener("rx", listener);
		// won't work in comfortable time..
		env.getConnectedToByName("rx")
		   .stream().flatMap(m -> env.getConnectedToByName(m.getName()).stream())
		   .forEach(m -> {
			   min.put(m.getName(), 0L);
			   env.addListener(m.getName(), listener);
		   });
		while (min.values().stream().anyMatch(a -> a == 0L)) {
			counter.incrementAndGet();
			env.initiate();
		}
		return "fewest button pushes = %d".formatted(min.values().stream().reduce(1L, Utils::lcm));
	}

}

