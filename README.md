## **The plugin only works on 1.8.x!**

## **Setup:** You just download the `LogEvent.jar` file, put it in your server's `plugins` folder, and restart or reload the server.

### (The plugin is designed for Spigot/Bukkit 1.8.x, but works perfectly with Paper too)

## **How to use the plugin:**
Make sure you have **OP** or the **logevent.use** permission, and then use the `/logevent` command.

## **Commands:**

### - `/logevent start <minutes> [end distance] [start distance]`
   - `minutes`: How long the event will last, in minutes (e.g., `60` for 1 hour).
   - `end distance`: (Optional) The final size of the world border (in blocks) at the end of the event.
   - `start distance`: (Optional) The initial size of the world border (in blocks) at the start of the event.
     - **Note:** If `end distance` and `start distance` are provided, the world border will smoothly shrink from `start distance` to `end distance` over 90% of the event duration.
     - **Note:** If `end distance` and `start distance` are excluded, the world border will remain static.
     - **Note:** When the command is run, all players will be killed, and the start message will be broadcasted to everyone.

### - `/logevent stop`
   - Stops any currently running log event.
   - Displays the log collection results for each player in chat.

### - `/logevent help`
   - Displays this help message, including plugin author and version information.

## **Description:**

This plugin creates a timed log collection event. When started, it shrinks the world border (if configured), forcing players to gather logs within a decreasing area. At the end of the event, the plugin calculates and broadcasts how many logs each player collected.

## **Features:**

-   Timed log collection events.
-   Configurable event duration and border size.
-   Optional automatic world border shrinking.
-   Player log collection counting.
-   In-game action bar timer display.
-   Chat broadcasts for event start, stop, and time remaining.
-   Console command support.
-   Help command with plugin information.
-   Kills all players on event start.
-   Sets the world border to a start distance before shrinking it.

## **Permissions:**

-   `logevent.use`: Allows players to use the `/logevent` command. (OPs have this by default)

## **Author:**

-   ThePingu

## **Version:**

-   (Automatically displays the version from your plugin.yml file using the /logevent help command)
