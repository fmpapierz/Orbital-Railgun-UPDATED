# 2.0.3+26.2

- Removed the vertical blue particle stream above the target.
- Replaced the fixed 300-block targeting cap with render-distance targeting on both client and server. Includes diagonal and elevated terrain without generating unloaded chunks.
- Added a local Impact camera shake toggle and 0–100% intensity slider, defaulting to 35%. Shake begins at impact, decays over three seconds and does not alter player movement or aim.
- Added real-client checks for a synchronized shot 480 blocks away and for the camera shake toggle and zero intensity.

# 2.0.2+26.2

- Added all original server settings to Gameplay settings: damage, sound/effect range, cooldown, active strike limit, particles and debug logging.
- Added independent crater and pull radius controls. Each strike captures its size for consistent rendering, damage and block removal.
- Validate numeric values on both client and server and broadcast authoritative changes. Added `/ore strikeRadius` and `/ore pullRadius`.
- Fade strike lighting, beam and chromatic distortion over the final five seconds, reaching the untouched scene before effect cleanup.
- Expanded packet and fade tests plus real-client menu and configurable crater checks. Network protocol updated; use matching client/server versions.

# 2.0.1+26.2

- Centered and scaled the inventory model to fit its slot.
- Restored scope opening from a horizontal line using item-use time.
- Moved post processing before the hand depth clear and captured the actual terrain projection. Restored rotating crosshair, evolving strike marker and terrain waves; beam stays at the strike's world position.
- Corrected the original shockwave clamp's argument order.
- Restricted pull to 24 blocks around the strike, with creative/spectator immunity.
- Added server-owned Enable pull and Destroy bedrock settings to the native configuration screen, plus `/ore pull` and `/ore bedrock`. Only the host or operators can change them.
- Expanded real-client checks with phased screenshots, camera movement, settings acknowledgements, player pull scenarios, and bedrock on/off behavior.
