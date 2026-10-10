# Pots

Op-only plugin for Paper 1.21.4+.

1. An op runs `/pots`.
2. A GUI opens. Left click a potion to select or unselect it (selected ones glow).
3. Right click a potion to switch it between level I and level II.
4. Click the lime **Start** button.
5. A 3 second countdown shows on everyone's screen.
6. Everyone online gets the selected potion effects.

## Durations

- Level I lasts 8 minutes
- Level II lasts 1 minute 30 seconds
- Weaving has no levels and always lasts 180 seconds (3 minutes)

## Build

You need Java 21 and Maven.

    mvn package

The jar is `target/Pots.jar`. Put it in your server's `plugins` folder and restart.

## Build on GitHub (no setup needed)

Every push builds the jar automatically:

1. Open the repo on GitHub and click the **Actions** tab.
2. Click the latest **Build** run.
3. Download **Pots** from the Artifacts section at the bottom. It contains `Pots.jar`.

## Releases

To get a normal download link:

1. On GitHub, click **Releases**, then **Create a new release**.
2. Type a tag like `v1.0.0` and click **Publish release**.
3. Wait a minute. `Pots.jar` is attached to the release automatically.

## Settings

After the first start, edit `plugins/Pots/config.yml`:

- `countdown-seconds` - length of the countdown (default 3)
- `duration-seconds.level-1` / `level-2` - how long each level lasts, in seconds
