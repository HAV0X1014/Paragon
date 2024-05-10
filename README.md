![banner](https://user-images.githubusercontent.com/85251388/179023488-0ade188b-840e-48c5-8fdf-3502ff2aa26e.png)

[![Downloads](https://img.shields.io/github/downloads/Wolfsurge/Paragon/total?color=blueviolet&style=for-the-badge)](https://github.com/Wolfsurge/Paragon/releases)
[![Discord](https://img.shields.io/discord/936976249300086854?color=blueviolet&label=Discord&logo=Discord&style=for-the-badge)](https://discord.gg/28JNQsXUzb)
![Lines of code](https://img.shields.io/tokei/lines/github/Wolfsurge/Paragon?color=blueviolet&label=lines%20of%20code&style=for-the-badge)

# Paragon

A 1.12.2 Anarchy Client. Probably never going to be worked on again, although I will test and merge any pull requests that are submitted.
Download the client from the latest
commit [here](https://nightly.link/Wolfsurge/Paragon/workflows/build/master/Paragon-Build.zip)

# FAQ

<details>
  <summary> How do I open the ClickGUI? </summary>

> The default ClickGUI bind is `RSHIFT`
</details>

<details>
  <summary> What does the GUI look like? </summary>
  
  ![image](https://user-images.githubusercontent.com/73380591/201485882-ae80c44d-8035-40de-985f-2de214630e35.png)
</details>

<details>
  <summary> How do I use commands? </summary>

> The command prefix is `$`, and you can run `$help` to get a list of all commands
</details>

<details>
  <summary> How do I request help, or suggest a feature? </summary>

> You can join the discord server (click on the badge with the online members in the discord) and use the appropriate channels
</details>

# Contributors

Surge <br>
Teletofu <br>
SooStrator <br>
Gentleman <br>
Doogie13 <br>
Sxmurai / Aesthetical <br>
EBSmash <br>
Bush <br>
Master7720 <br>
Chanakan55991 <br>
Swp <br>
Xello12121 <br>
Cart69420

## Other Credits

Files will have an @author tag just before the class declaration to tell you who wrote it.

# Build instructions

Windows:

`.\gradlew build`

Linux/Mac (or any other based UNIX/UNIX-LIKE OS):

`chmod +x gradlew`

`./gradlew build`

Note that this may not produce a functioning build, as Mixin's annotation processor does not
always work correctly.

# Contributing

Fork the repository and push your changes to the fork. Then, create a pull request to this repository and it will be
reviewed.

Follow the [java](https://www.oracle.com/technetwork/java/codeconventions-150003.pdf)
and [kotlin](https://kotlinlang.org/docs/coding-conventions.html) coding conventions and try to adapt the code style
found in the rest of the client, such as using `lowerCamelCase` and using the 1TBS brace style.

# Using the Custom Font

A directory is created in the Paragon config folder (/.minecraft/paragon), called "font". Here, upon first opening the
client, two files will be present.
"font.ttf" is the font that will be used when the Font module is enabled. The second file is "font_config.json". Here
you can find a couple of font rendering
settings. "size" is an integer (whole number) value that determines the font's size. "y_offset" is a float (decimal)
value to determine how much to add to the Y value when text is rendered. (this is so you can center it so it looks
good (*cough* wp3 *cough*).

You can change the custom font by simply deleting "font.ttf" in the font folder, and replacing it with a different TTF
font file, which is also called "font.ttf". It must be named this as otherwise the client will not detect the font, and
will therefore default to Java's default font. Support for fonts with different file names might be added in the future.
