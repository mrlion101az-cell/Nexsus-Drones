# NexusDrones v0.1.0

Drones and turrets built entirely on ArmorStands + custom tick logic --
no resource pack, no new mob models.

## What's built
- **Surveillance Drone** -- patrols in a circle, spots enemy players in a
  20-block radius, gives them Glowing + pings the owner in chat (now on
  the same adjustable cooldown as everything else -- see below, this
  used to have zero throttling and could spam chat 10x/sec).
- **Kamikaze Drone** -- homes in on the nearest enemy player, detonates a
  real explosion (power 3.0) within 2 blocks.
- **Sentry Turret** -- stationary, holds a crossbow, rotates to face the
  nearest enemy in a 25-block radius, fires real arrows every 1.5s.

## Commands
- `/nexusdrones give surveillance`
- `/nexusdrones give kamikaze`
- `/nexusdrones give turret`
- `/nexusdrones count` -- shows how many drones/turrets are currently active
- `/nexusdrones removeall` -- wipes every active drone/turret (use this
  liberally while testing -- it's your panic button)

## How to deploy one
Right-click while holding a given item to spawn it at your location.
The item is consumed on use.

## Setup (same flow as NexusMechanica)
1. Push this folder to a new GitHub repo (or a new folder in your
   existing NexusMechanica repo -- but a separate repo is cleaner since
   this is a separate plugin/jar).
2. Open it in a Codespace.
3. Confirm Java 21 is active: `java -version` -- if not, `sdk use java 21.0.11-amzn`
   (same fix as last time, should already be installed in this Codespace).
4. `mvn clean package`
5. Grab `target/NexusDrones-0.1.0.jar`, drop it in your server's
   `plugins/` folder, restart.

## Known rough edges to expect (this is a first pass, un-tested in-game)
- **No team/faction check yet** -- right now "enemy" just means "any
  online player who isn't the owner." If two allies are both testing,
  they'll target each other. Say the word and I'll wire in a
  scoreboard-team check next.
- **Kamikaze pathing is a straight line** -- it doesn't avoid terrain,
  so it can get stuck on a wall between it and the target. Fine over
  open ground / in the air, less fine indoors.
- **Turret has no line-of-sight check** -- it'll try to fire through
  walls if a target is within radius. Also a quick add if it's a problem.
- **No despawn/lifetime limit** -- drones and turrets live forever until
  killed or `/nexusdrones removeall`. Worth adding a timer once we know
  how long a "deployment" should last in your war system.
- **Explosion griefs terrain** -- `createExplosion` breaks blocks by
  default (that's the `true` at the end). If you want kamikaze drones
  to only hurt players/entities without cratering builds, that's a
  one-line change to `false`.

Deploy it, break it, and send me whatever chat log / behavior looks
wrong -- exact same loop as the guide book bug.

## Security Suite (added on top of drones)

Same jar, new commands: `/nexussecurity`.

- **Security Camera** (`/nexussecurity give camera`) -- right-click a
  block to mount a named, stationary camera. Scans a 15-block radius,
  pings you in chat when it spots someone who isn't you.
- **Security Tablet** (`/nexussecurity give tablet`) -- right-click to
  list your cameras. `/nexussecurity view <name>` teleports you to that
  camera's spot to look around; `/nexussecurity return` sends you back
  to exactly where you were.
- **Laser Post** (`/nexussecurity give laser`) -- right-click two blocks
  to connect a visible red particle beam between them. Anyone but you
  who crosses it trips an alarm straight to your chat.
- **Ghost Door** (`/nexussecurity give ghostdoor`) -- right-click an
  *existing* placed block to register it as a secret door. Sneak +
  right-click that same block afterward to toggle it between solid and
  passable.

### Alert cooldown (fixed this round)
Camera alerts, laser alarms, and surveillance drone "spotted" messages
all now read from one shared, per-player, adjustable cooldown instead
of three separate hardcoded numbers (camera was 10s, laser was 5s, and
the surveillance drone had **no cooldown at all** -- it could send a
chat message up to 10 times a second for as long as someone stayed in
range, which is the real bug behind "the message system is set to
infinite").

- `/nexussecurity cooldown <seconds>` -- sets your own cooldown (applies
  to all of your cameras, lasers, and surveillance drones together).
  Run with no number to see your current setting. 1 second is the
  floor, no unlimited/zero spam allowed.
- It's per-player, not server-wide -- adjusting yours doesn't affect
  anyone else's devices.

### Ignore list (new)
- `/nexussecurity ignore <player>` -- toggles that player off (or back
  on) your cameras' alert list. Ignored players still show up if you
  physically look at the camera feed via `/nexussecurity view`, they
  just won't trigger chat pings. Works on offline players too (by
  name). Anyone can use this on their own cameras -- no permission
  needed, same as the rest of `/nexussecurity`.
- Currently camera-only, not wired into lasers or drones -- say the
  word if you want the same ignore list to apply there too.

### The ghost door caveat (read this before you get excited)
This is a **shared toggle**, not true SecurityCraft-style per-player
ghosting. When you open it, it's open for *everyone* -- intruders
included -- until you close it again. Real "solid wall for them,
walk-through for me" requires sending different block states to
different players, which needs packet-level tools like ProtocolLib that
aren't in plain Paper API. If that's a dealbreaker, tell me and we can
scope that as a real phase-2 feature with the added dependency -- just
didn't want to quietly ship something that looks like it does that and
doesn't.

### Other rough edges
- Cameras are lightweight (no entity spawned, just a tracked point) --
  cheap to scale up to a big network.
- Camera names auto-generate as `cam1`, `cam2`, etc. per player; no
  rename command yet.
- Laser beams redraw every tick the manager ticks, so expect a decent
  amount of particle traffic if you place a lot of them -- flag it if
  it gets laggy and I'll throttle the redraw rate.
- No persistence yet for any of these three (cameras/lasers/doors reset
  on restart) -- same fix as NexusFortify (save to config.yml) if you
  want it, just didn't duplicate that work until you confirm the
  mechanics feel right first.
- Same applies to cooldown settings and ignore lists -- both reset on
  restart right now, for the same reason.
- No permission gating anywhere in this plugin yet -- every command
  (including `give`, which hands out real explosive drones) is open to
  every player by default. Fine for a closed testing group; worth
  locking down with real permissions before this goes to your wider
  playerbase.
