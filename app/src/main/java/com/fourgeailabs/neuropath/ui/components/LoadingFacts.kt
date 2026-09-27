package com.fourgeailabs.neuropath.ui.components

/**
 * Bite-size learning nibbles shown on the AI model loading screen.
 *
 * These are baked in (not generated) on purpose: the local AI model is still
 * loading while this screen is visible, so there is no engine available to
 * write facts yet. Every fact is a short, kid-friendly "history nibble"
 * about one of the app's learning modules, flavoured for the active theme.
 */
data class LoadingFact(
    val text: String,
    val module: String
)

private fun fact(text: String, module: String) = LoadingFact(text, module)

private val DINO_FACTS = listOf(
    fact("The word \u201cdinosaur\u201d was invented in 1842 by scientist Richard Owen. It means \u201cterrible lizard\u201d in ancient Greek!", "Science"),
    fact("A T. rex had about 60 teeth, each as long as a banana. When one fell out, a new one grew back \u2014 just like a shark!", "Math"),
    fact("The first dinosaur ever given a name was Megalosaurus, in 1824, from a giant jawbone found in England.", "History"),
    fact("Mary Anning found sea-monster skeletons as a young girl, but men took credit for her discoveries for 100 years.", "Reading"),
    fact("Birds are living dinosaurs! A chicken is the closest living cousin of the mighty T. rex.", "Science"),
    fact("The biggest dinosaur eggs were the size of basketballs. Imagine finding THAT in your backyard!", "Science")
)

private val SPACE_FACTS = listOf(
    fact("The first living traveler in space was a dog named Laika, who orbited Earth in 1957.", "History"),
    fact("Sunlight takes 8 minutes to reach Earth. If the Sun vanished, we wouldn\u2019t know for 8 whole minutes!", "Math"),
    fact("In 1969, Apollo 11 landed on the Moon. 600 million people watched Neil Armstrong\u2019s first step.", "History"),
    fact("A day on Venus is longer than its whole year \u2014 it spins that slowly!", "Science"),
    fact("The word \u201castronaut\u201d comes from Greek words meaning \u201cstar sailor\u201d.", "Reading"),
    fact("There are more stars in the universe than grains of sand on all of Earth\u2019s beaches.", "Math")
)

private val SUPERHERO_FACTS = listOf(
    fact("Superman first appeared in 1938 in a comic that cost 10 cents. One copy later sold for over $3 million!", "History"),
    fact("Scientists really study \u201csuperhero physics\u201d \u2014 like how much power it would take to actually fly.", "Science"),
    fact("Every comic book is teamwork: a writer, a penciler, an inker, and a colorist all build it together.", "Art"),
    fact("The first Black superhero in mainstream comics, Black Panther, debuted in 1966.", "Reading"),
    fact("Stan Lee said superheroes teach us that with great power comes great responsibility \u2014 to be kind.", "SEL"),
    fact("Wonder Woman was created to show that strength and compassion belong together.", "SEL")
)

private val OCEAN_FACTS = listOf(
    fact("The Mariana Trench, the deepest part of the ocean, is deeper than Mount Everest is tall!", "Science"),
    fact("Old sailors drew sea monsters on maps where the ocean was unexplored. Now we\u2019ve mapped the whole seafloor.", "History"),
    fact("An octopus has 3 hearts and blue blood. Two hearts pause when it swims \u2014 that\u2019s why they prefer to crawl!", "Math"),
    fact("The word \u201ctsunami\u201d comes from Japanese and means \u201charbor wave\u201d.", "Reading"),
    fact("Most of the ocean has never been explored. We know more about the Moon\u2019s surface!", "Science"),
    fact("Sea otters hold hands while they sleep so they don\u2019t drift apart. Friendship is survival!", "SEL")
)

private val KNIGHT_FACTS = listOf(
    fact("A knight\u2019s armor could weigh as much as a second-grader \u2014 about 50 pounds!", "History"),
    fact("Castle walls were built extra thick at the bottom so enemy tunnels dug underneath would collapse.", "Science"),
    fact("Knights trained for about 14 years, starting as a page at age 7. That\u2019s longer than elementary AND middle school!", "Math"),
    fact("The word \u201cknight\u201d comes from an old word meaning \u201cboy\u201d or \u201cservant\u201d.", "Reading"),
    fact("Castle staircases spiraled clockwise so right-handed defenders swinging down had the advantage.", "History"),
    fact("A squire\u2019s most important job was caring for the knight\u2019s horse \u2014 trust starts with responsibility.", "SEL")
)

private val MAGIC_FACTS = listOf(
    fact("Stage magic is thousands of years old \u2014 Egyptian magicians did cup-and-ball tricks 4,000 years ago!", "History"),
    fact("Magicians use real science: mirrors, angles, and the way your brain fills in gaps in what it sees.", "Science"),
    fact("The word \u201cabracadabra\u201d was once believed to be a real healing spell, worn on lucky charms.", "Reading"),
    fact("Many card tricks hide secret math \u2014 like knowing every 7th card lands in the same spot after a shuffle.", "Math"),
    fact("Great magicians practice one trick hundreds of times. Mistakes are just rehearsal in disguise!", "SEL"),
    fact("Harry Houdini\u2019s real name was Ehrich Weiss. He picked a stage name, and so can you pick your story.", "Reading")
)

private val GAMES_FACTS = listOf(
    fact("The first video game, \u201cTennis for Two,\u201d was built in 1958 by a physicist on an oscilloscope!", "History"),
    fact("Chess has more possible games than there are atoms in the universe. Mathematicians proved it!", "Math"),
    fact("Strategy games grow the part of your brain that plans ahead. Play is practice!", "Science"),
    fact("The word \u201cgame\u201d is over 1,000 years old and once meant \u201cjoy\u201d.", "Reading"),
    fact("Ancient Egyptians played Senet 5,000 years ago \u2014 older than the paint on the pyramids!", "History"),
    fact("Losing a game teaches your brain more than winning does. Every \u201cgame over\u201d is a lesson.", "SEL")
)

private val MYTHICAL_FACTS = listOf(
    fact("Dragons appear in stories from China to Wales \u2014 cultures that never met all imagined them!", "Reading"),
    fact("The ancient Greeks believed unicorns were real animals living in faraway India.", "History"),
    fact("Narwhals \u2014 real whales with a spiral tusk \u2014 may be where unicorn legends began.", "Science"),
    fact("Mythical beasts are mashups: a griffin is an eagle\u2019s head on a lion\u2019s body.", "Art"),
    fact("Myths were the first self-help books \u2014 heroes show us how to be brave when we\u2019re scared.", "SEL"),
    fact("The phoenix burns up and is reborn from ashes \u2014 a story about starting over, told for 2,000 years.", "Reading")
)

private val ROBOTICS_FACTS = listOf(
    fact("The word \u201crobot\u201d comes from a 1920 play and means \u201cforced labor\u201d in Czech.", "Reading"),
    fact("The first robot arm, Unimate, got a job in a car factory in 1961 \u2014 and never asked for a lunch break.", "History"),
    fact("Robots think in 1s and 0s. The number 13 in robot-talk is 1101!", "Math"),
    fact("Isaac Asimov wrote three laws to keep robots friendly \u2014 in 1942, before real robots existed!", "Reading"),
    fact("Every robot follows a program: a recipe of steps. You think like a programmer when you follow a recipe!", "Coding"),
    fact("The Mars rovers are robots the size of cars, driving themselves on another planet right now.", "Science")
)

private val TRAIN_FACTS = listOf(
    fact("The first steam train carried passengers in 1825 at 15 miles per hour \u2014 slower than a bicycle sprint!", "History"),
    fact("Maglev trains float on magnets and can hit 375 miles per hour \u2014 faster than a race car!", "Science"),
    fact("A freight train can stretch 2 miles long with 200 cars. Counting them takes real patience!", "Math"),
    fact("The word \u201clocomotive\u201d means \u201cmoving from its place\u201d in Latin.", "Reading"),
    fact("Trains changed time itself \u2014 towns had to agree on time zones so schedules would work!", "History"),
    fact("The Trans-Siberian Railway is the longest in the world: 5,772 miles across Russia.", "Math")
)

private val ANIME_FACTS = listOf(
    fact("Anime is drawn at about 12 pictures per second \u2014 a 20-minute episode needs over 14,000 drawings!", "Art"),
    fact("Japan\u2019s first animated film was made in 1917, cut from paper like shadow puppets.", "History"),
    fact("Manga reads right-to-left, the traditional Japanese way \u2014 your brain learns a whole new reading dance!", "Reading"),
    fact("Animators study real physics so jumping characters feel weighty and true.", "Science"),
    fact("Big shiny anime eyes were inspired by early Disney cartoons \u2014 art travels across oceans!", "Art"),
    fact("Studio Ghibli\u2019s films are hand-drawn with watercolor backgrounds, frame by frame.", "Art")
)

private val WILDLIFE_FACTS = listOf(
    fact("A group of flamingos is called a \u201cflamboyance\u201d \u2014 the fanciest word in nature!", "Reading"),
    fact("A honeybee visits 50 to 100 flowers per trip, yet makes only 1/12 of a teaspoon of honey in its whole life.", "Math"),
    fact("Ancient Egyptians loved cats so much they made them gods \u2014 and mummified them as family.", "History"),
    fact("\u201cHippopotamus\u201d means \u201criver horse\u201d in Greek. They can\u2019t swim \u2014 they bounce along the riverbed!", "Reading"),
    fact("Elephants comfort sad friends by touching trunks. Even animals know kindness matters.", "SEL"),
    fact("A chameleon\u2019s tongue can be twice as long as its body \u2014 the fastest lunch-grab in nature.", "Science")
)

private val SCIENCE_FACTS = listOf(
    fact("Marie Curie won Nobel Prizes in TWO sciences \u2014 physics AND chemistry. Nobody else ever has!", "History"),
    fact("Your body builds 25 million new cells every second. You are literally new all the time!", "Science"),
    fact("Zero was invented in ancient India. Before zero, math was like counting with no empty box!", "Math"),
    fact("The word \u201cscience\u201d comes from a Latin word meaning \u201cto know\u201d.", "Reading"),
    fact("The first microscope was built around 1590, opening a universe too small to see.", "History"),
    fact("Ada Lovelace wrote the first computer program in the 1840s \u2014 a century before computers!", "Coding")
)

private val MUSIC_FACTS = listOf(
    fact("The oldest known song was written on a clay tablet 3,400 years ago \u2014 a hymn from ancient Syria.", "History"),
    fact("Your heartbeat naturally syncs to music you love. That\u2019s why concerts feel electric!", "Science"),
    fact("Music IS math: a beat split in half, then half again, builds the rhythm of your favorite song.", "Math"),
    fact("The word \u201cpiano\u201d is short for \u201cpianoforte\u201d \u2014 Italian for \u201csoft-loud\u201d.", "Reading"),
    fact("Singing in a group releases happy chemicals in your brain. Choirs are joy machines!", "SEL"),
    fact("Beethoven wrote his greatest music after going deaf \u2014 he felt the vibrations through the floor.", "History")
)

/** Fallback nibbles for any theme without its own set. */
private val GENERAL_FACTS = listOf(
    fact("The abacus is 4,000 years old and still faster than a calculator in trained hands!", "Math"),
    fact("The printing press, invented around 1440, made books cheap enough for ordinary kids to read.", "Reading"),
    fact("Honey never spoils. Archaeologists tasted 3,000-year-old honey from Egyptian tombs \u2014 still sweet!", "Science"),
    fact("The first dictionary took one man, Samuel Johnson, 9 years to write \u2014 by hand.", "Reading"),
    fact("Ancient Romans used urine to whiten their togas. History is weird and wonderful!", "History"),
    fact("The Great Wall of China is held together in places with sticky rice mortar.", "Science"),
    fact("Vikings used sunstones \u2014 glowing crystals \u2014 to navigate on cloudy days.", "History"),
    fact("Taking three slow breaths calms your brain faster than almost anything else. Try it now!", "SEL")
)

val LOADING_FACTS_BY_THEME: Map<String, List<LoadingFact>> = mapOf(
    "dino" to DINO_FACTS,
    "space" to SPACE_FACTS,
    "superhero" to SUPERHERO_FACTS,
    "ocean" to OCEAN_FACTS,
    "knights" to KNIGHT_FACTS,
    "magic" to MAGIC_FACTS,
    "games" to GAMES_FACTS,
    "mythical" to MYTHICAL_FACTS,
    "robotics" to ROBOTICS_FACTS,
    "trains" to TRAIN_FACTS,
    "anime" to ANIME_FACTS,
    "wildlife" to WILDLIFE_FACTS,
    "science" to SCIENCE_FACTS,
    "music" to MUSIC_FACTS
)

/** Theme-flavoured facts when available, general learning nibbles otherwise. */
fun loadingFactsForTheme(themeId: String): List<LoadingFact> =
    LOADING_FACTS_BY_THEME[themeId] ?: GENERAL_FACTS

/** How long each nibble stays on screen before crossfading to the next. */
const val LOADING_FACT_ROTATION_MS = 6_000L
