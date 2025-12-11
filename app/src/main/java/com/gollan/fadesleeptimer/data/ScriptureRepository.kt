package com.gollan.fadesleeptimer.data

data class ScriptureData(
    val text: String,
    val reference: String,
    val url: String
)

object ScriptureRepository {
    
    val categories = mapOf(
        "The Classics" to listOf(
            ScriptureData("In peace I will lie down and sleep, for you alone, Lord, make me dwell in safety.", "Psalm 4:8", "https://www.bible.com/bible/111/PSA.4.NIV"),
            ScriptureData("I lie down and sleep; I wake again, because the Lord sustains me.", "Psalm 3:5", "https://www.bible.com/bible/111/PSA.3.NIV"),
            ScriptureData("When you lie down, you will not be afraid; when you lie down, your sleep will be sweet.", "Proverbs 3:24", "https://www.bible.com/bible/111/PRO.3.NIV"),
            ScriptureData("In vain you rise early and stay up late, toiling for food to eat - for he grants sleep to those he loves.", "Psalm 127:2", "https://www.bible.com/bible/111/PSA.127.NIV"),
            ScriptureData("Come to me, all you who are weary and burdened, and I will give you rest.", "Matthew 11:28", "https://www.bible.com/bible/111/MAT.11.NIV"),
            ScriptureData("Take my yoke upon you and learn from me... and you will find rest for your souls.", "Matthew 11:29-30", "https://www.bible.com/bible/111/MAT.11.NIV"),
            ScriptureData("The Lord replied, 'My Presence will go with you, and I will give you rest.", "Exodus 33:14", "https://www.bible.com/bible/111/EXO.33.NIV"),
            ScriptureData("I will refresh the weary and satisfy the faint.", "Jeremiah 31:25", "https://www.bible.com/bible/111/JER.31.NIV"),
            ScriptureData("Return to your rest, my soul, for the Lord has been good to you.", "Psalm 116:7", "https://www.bible.com/bible/111/PSA.116.NIV"),
            ScriptureData("There remains, then, a Sabbath-rest for the people of God...", "Hebrews 4:9-10", "https://www.bible.com/bible/111/HEB.4.NIV")
        ),
        "Anxiety & Peace" to listOf(
            ScriptureData("Cast all your anxiety on him because he cares for you.", "1 Peter 5:7", "https://www.bible.com/bible/111/1PE.5.NIV"),
            ScriptureData("Do not be anxious about anything, but in every situation, by prayer and petition, with thanksgiving, present your requests to God.", "Philippians 4:6", "https://www.bible.com/bible/111/PHP.4.NIV"),
            ScriptureData("And the peace of God, which transcends all understanding, will guard your hearts and your minds in Christ Jesus.", "Philippians 4:7", "https://www.bible.com/bible/111/PHP.4.NIV"),
            ScriptureData("You will keep in perfect peace those whose minds are steadfast, because they trust in you.", "Isaiah 26:3", "https://www.bible.com/bible/111/ISA.26.NIV"),
            ScriptureData("When anxiety was great within me, your consolation brought me joy.", "Psalm 94:19", "https://www.bible.com/bible/111/PSA.94.NIV"),
            ScriptureData("Peace I leave with you; my peace I give you... Do not let your hearts be troubled and do not be afraid.", "John 14:27", "https://www.bible.com/bible/111/JHN.14.NIV"),
            ScriptureData("Now may the Lord of peace himself give you peace at all times and in every way.", "2 Thessalonians 3:16", "https://www.bible.com/bible/111/2TH.3.NIV"),
            ScriptureData("Cast your cares on the Lord and he will sustain you; he will never let the righteous be shaken.", "Psalm 55:22", "https://www.bible.com/bible/111/PSA.55.NIV"),
            ScriptureData("Therefore do not worry about tomorrow, for tomorrow will worry about itself. Each day has enough trouble of its own.", "Matthew 6:34", "https://www.bible.com/bible/111/MAT.6.NIV"),
            ScriptureData("When I am afraid, I put my trust in you.", "Psalm 56:3", "https://www.bible.com/bible/111/PSA.56.NIV")
        ),
        "The Shepherd" to listOf(
            ScriptureData("The Lord is my shepherd, I lack nothing.", "Psalm 23:1", "https://www.bible.com/bible/111/PSA.23.NIV"),
            ScriptureData("He makes me lie down in green pastures, he leads me beside quiet waters.", "Psalm 23:2", "https://www.bible.com/bible/111/PSA.23.NIV"),
            ScriptureData("He refreshes my soul. He guides me along the right paths for his name's sake.", "Psalm 23:3", "https://www.bible.com/bible/111/PSA.23.NIV"),
            ScriptureData("Even though I walk through the darkest valley, I will fear no evil, for you are with me; your rod and your staff, they comfort me.", "Psalm 23:4", "https://www.bible.com/bible/111/PSA.23.NIV"),
            ScriptureData("Surely your goodness and love will follow me all the days of my life...", "Psalm 23:6", "https://www.bible.com/bible/111/PSA.23.NIV"),
            ScriptureData("I am the good shepherd; I know my sheep and my sheep know me.", "John 10:14-15", "https://www.bible.com/bible/111/JHN.10.NIV"),
            ScriptureData("My sheep listen to my voice; I know them, and they follow me. I give them eternal life...", "John 10:27-28", "https://www.bible.com/bible/111/JHN.10.NIV"),
            ScriptureData("He tends his flock like a shepherd: he gathers the lambs in his arms and carries them close to his heart.", "Isaiah 40:11", "https://www.bible.com/bible/111/ISA.40.NIV"),
            ScriptureData("Know that the Lord is God. It is he who made us, and we are his; we are his people, the sheep of his pasture.", "Psalm 100:3", "https://www.bible.com/bible/111/PSA.100.NIV"),
            ScriptureData("For 'you were like sheep going astray,' but now you have returned to the Shepherd and Overseer of your souls.", "1 Peter 2:25", "https://www.bible.com/bible/111/1PE.2.NIV")
        ),
        "Divine Protection" to listOf(
            ScriptureData("I lift up my eyes to the mountains - where does my help come from? My help comes from the Lord, the Maker of heaven and earth.", "Psalm 121:1-2", "https://www.bible.com/bible/111/PSA.121.NIV"),
            ScriptureData("He will not let your foot slip - he who watches over you will not slumber...", "Psalm 121:3-4", "https://www.bible.com/bible/111/PSA.121.NIV"),
            ScriptureData("The Lord watches over you... the sun will not harm you by day, nor the moon by night.", "Psalm 121:5-6", "https://www.bible.com/bible/111/PSA.121.NIV"),
            ScriptureData("The Lord will keep you from all harm... the Lord will watch over your coming and going both now and for evermore.", "Psalm 121:7-8", "https://www.bible.com/bible/111/PSA.121.NIV"),
            ScriptureData("Whoever dwells in the shelter of the Most High will rest in the shadow of the Almighty.", "Psalm 91:1-2", "https://www.bible.com/bible/111/PSA.91.NIV"),
            ScriptureData("He will cover you with his feathers, and under his wings you will find refuge...", "Psalm 91:4", "https://www.bible.com/bible/111/PSA.91.NIV"),
            ScriptureData("For he will command his angels concerning you to guard you in all your ways.", "Psalm 91:11", "https://www.bible.com/bible/111/PSA.91.NIV"),
            ScriptureData("The name of the Lord is a fortified tower; the righteous run to it and are safe.", "Proverbs 18:10", "https://www.bible.com/bible/111/PRO.18.NIV"),
            ScriptureData("The Lord is my rock, my fortress and my deliverer; my God is my rock, in whom I take refuge.", "Psalm 18:2", "https://www.bible.com/bible/111/PSA.18.NIV"),
            ScriptureData("You are my hiding-place; you will protect me from trouble and surround me with songs of deliverance.", "Psalm 32:7", "https://www.bible.com/bible/111/PSA.32.NIV"),
            ScriptureData("God is our refuge and strength, an ever-present help in trouble.", "Psalm 46:1", "https://www.bible.com/bible/111/PSA.46.NIV"),
            ScriptureData("The eternal God is your refuge, and underneath are the everlasting arms.", "Deuteronomy 33:27", "https://www.bible.com/bible/111/DEU.33.NIV"),
            ScriptureData("As the mountains surround Jerusalem, so the Lord surrounds his people both now and for evermore.", "Psalm 125:2", "https://www.bible.com/bible/111/PSA.125.NIV"),
            ScriptureData("But the Lord is faithful, and he will strengthen you and protect you from the evil one.", "2 Thessalonians 3:3", "https://www.bible.com/bible/111/2TH.3.NIV")
        ),
        "Forgiveness & Grace" to listOf(
            ScriptureData("Because of the Lord's great love we are not consumed, for his compassions never fail. They are new every morning...", "Lamentations 3:22-23", "https://www.bible.com/bible/111/LAM.3.NIV"),
            ScriptureData("If we confess our sins, he is faithful and just and will forgive us our sins and purify us from all unrighteousness.", "1 John 1:9", "https://www.bible.com/bible/111/1JN.1.NIV"),
            ScriptureData("He does not treat us as our sins deserve or repay us according to our iniquities.", "Psalm 103:10", "https://www.bible.com/bible/111/PSA.103.NIV"),
            ScriptureData("As far as the east is from the west, so far has he removed our transgressions from us.", "Psalm 103:12", "https://www.bible.com/bible/111/PSA.103.NIV"),
            ScriptureData("Therefore, there is now no condemnation for those who are in Christ Jesus.", "Romans 8:1", "https://www.bible.com/bible/111/ROM.8.NIV"),
            ScriptureData("Though your sins are like scarlet, they shall be as white as snow.", "Isaiah 1:18", "https://www.bible.com/bible/111/ISA.1.NIV"),
            ScriptureData("You will again have compassion on us; you will tread our sins underfoot and hurl all our iniquities into the depths of the sea.", "Micah 7:19", "https://www.bible.com/bible/111/MIC.7.NIV"),
            ScriptureData("In him we have redemption through his blood, the forgiveness of sins, in accordance with the riches of God's grace.", "Ephesians 1:7", "https://www.bible.com/bible/111/EPH.1.NIV"),
            ScriptureData("For I will forgive their wickedness and will remember their sins no more.", "Hebrews 8:12", "https://www.bible.com/bible/111/HEB.8.NIV"),
            ScriptureData("You, Lord, are forgiving and good, abounding in love to all who call to you.", "Psalm 86:5", "https://www.bible.com/bible/111/PSA.86.NIV")
        ),
        "God's Care" to listOf(
            ScriptureData("The Lord your God is with you, the Mighty Warrior who saves. He will take great delight in you; in his love he will no longer rebuke you, but will rejoice over you with singing.", "Zephaniah 3:17", "https://www.bible.com/bible/111/ZEP.3.NIV"),
            ScriptureData("You have searched me, Lord, and you know me. You know when I sit and when I rise; you perceive my thoughts from afar.", "Psalm 139:1-2", "https://www.bible.com/bible/111/PSA.139.NIV"),
            ScriptureData("If I say, 'Surely the darkness will hide me'... even the darkness will not be dark to you; the night will shine like the day.", "Psalm 139:11-12", "https://www.bible.com/bible/111/PSA.139.NIV"),
            ScriptureData("How precious to me are your thoughts, God... when I awake, I am still with you.", "Psalm 139:17-18", "https://www.bible.com/bible/111/PSA.139.NIV"),
            ScriptureData("You make known to me the path of life; you will fill me with joy in your presence.", "Psalm 16:11", "https://www.bible.com/bible/111/PSA.16.NIV"),
            ScriptureData("My flesh and my heart may fail, but God is the strength of my heart and my portion for ever.", "Psalm 73:26", "https://www.bible.com/bible/111/PSA.73.NIV"),
            ScriptureData("Do not be afraid; do not be discouraged, for the Lord your God will be with you wherever you go.", "Joshua 1:9", "https://www.bible.com/bible/111/JOS.1.NIV"),
            ScriptureData("Never will I leave you; never will I forsake you.", "Hebrews 13:5", "https://www.bible.com/bible/111/HEB.13.NIV"),
            ScriptureData("The Lord is my light and my salvation - whom shall I fear?", "Psalm 27:1", "https://www.bible.com/bible/111/PSA.27.NIV"),
            ScriptureData("So do not fear, for I am with you; do not be dismayed, for I am your God. I will strengthen you and help you...", "Isaiah 41:10", "https://www.bible.com/bible/111/ISA.41.NIV"),
            ScriptureData("For I am convinced that neither death nor life... will be able to separate us from the love of God that is in Christ Jesus our Lord.", "Romans 8:38-39", "https://www.bible.com/bible/111/ROM.8.NIV")
        ),
        "Hope & Assurance" to listOf(
            ScriptureData("Truly my soul finds rest in God; my salvation comes from him.", "Psalm 62:1", "https://www.bible.com/bible/111/PSA.62.NIV"),
            ScriptureData("Trust in him at all times, you people; pour out your hearts to him, for God is our refuge.", "Psalm 62:8", "https://www.bible.com/bible/111/PSA.62.NIV"),
            ScriptureData("I wait for the Lord, my whole being waits, and in his word I put my hope.", "Psalm 130:5", "https://www.bible.com/bible/111/PSA.130.NIV"),
            ScriptureData("I wait for the Lord more than watchmen wait for the morning, more than watchmen wait for the morning.", "Psalm 130:6", "https://www.bible.com/bible/111/PSA.130.NIV"),
            ScriptureData("In repentance and rest is your salvation, in quietness and trust is your strength.", "Isaiah 30:15", "https://www.bible.com/bible/111/ISA.30.NIV"),
            ScriptureData("The Lord is close to the broken-hearted and saves those who are crushed in spirit.", "Psalm 34:18", "https://www.bible.com/bible/111/PSA.34.NIV"),
            ScriptureData("He heals the broken-hearted and binds up their wounds.", "Psalm 147:3", "https://www.bible.com/bible/111/PSA.147.NIV"),
            ScriptureData("Therefore we do not lose heart. Though outwardly we are wasting away, yet inwardly we are being renewed day by day.", "2 Corinthians 4:16", "https://www.bible.com/bible/111/2CO.4.NIV"),
            ScriptureData("For our light and momentary troubles are achieving for us an eternal glory that far outweighs them all.", "2 Corinthians 4:17", "https://www.bible.com/bible/111/2CO.4.NIV"),
            ScriptureData("May the God of hope fill you with all joy and peace as you trust in him...", "Romans 15:13", "https://www.bible.com/bible/111/ROM.15.NIV"),
            ScriptureData("Why, my soul, are you downcast? ... Put your hope in God, for I will yet praise him, my Saviour and my God.", "Psalm 42:11", "https://www.bible.com/bible/111/PSA.42.NIV")
        ),
        "God's Strength" to listOf(
            ScriptureData("He gives strength to the weary and increases the power of the weak.", "Isaiah 40:29", "https://www.bible.com/bible/111/ISA.40.NIV"),
            ScriptureData("But those who hope in the Lord will renew their strength. They will soar on wings like eagles...", "Isaiah 40:31", "https://www.bible.com/bible/111/ISA.40.NIV"),
            ScriptureData("The Lord will vindicate me; your love, Lord, endures for ever.", "Psalm 138:8", "https://www.bible.com/bible/111/PSA.138.NIV"),
            ScriptureData("The Lord will watch over your coming and going both now and for evermore.", "Psalm 121:8", "https://www.bible.com/bible/111/PSA.121.NIV"),
            ScriptureData("The Lord gives strength to his people; the Lord blesses his people with peace.", "Psalm 29:11", "https://www.bible.com/bible/111/PSA.29.NIV"),
            ScriptureData("Your word is a lamp for my feet, a light on my path.", "Psalm 119:105", "https://www.bible.com/bible/111/PSA.119.NIV"),
            ScriptureData("You are my refuge and my shield; I have put my hope in your word.", "Psalm 119:114", "https://www.bible.com/bible/111/PSA.119.NIV"),
            ScriptureData("Now to him who is able to do immeasurably more than all we ask or imagine...", "Ephesians 3:20", "https://www.bible.com/bible/111/EPH.3.NIV"),
            ScriptureData("...He who began a good work in you will carry it on to completion until the day of Christ Jesus.", "Philippians 1:6", "https://www.bible.com/bible/111/PHP.1.NIV"),
            ScriptureData("For the Spirit God gave us does not make us timid, but gives us power, love and self-discipline.", "2 Timothy 1:7", "https://www.bible.com/bible/111/2TI.1.NIV"),
            ScriptureData("Do not fear, for I have redeemed you; I have summoned you by name; you are mine.", "Isaiah 43:1", "https://www.bible.com/bible/111/ISA.43.NIV"),
            ScriptureData("When you pass through the waters, I will be with you... when you walk through the fire, you will not be burned.", "Isaiah 43:2", "https://www.bible.com/bible/111/ISA.43.NIV")
        ),
        "Prayers & Benedictions" to listOf(
            ScriptureData("May my prayer be set before you like incense; may the lifting up of my hands be like the evening sacrifice.", "Psalm 141:2", "https://www.bible.com/bible/111/PSA.141.NIV"),
            ScriptureData("On my bed I remember you; I think of you through the watches of the night.", "Psalm 63:6", "https://www.bible.com/bible/111/PSA.63.NIV"),
            ScriptureData("Because you are my help, I sing in the shadow of your wings.", "Psalm 63:7", "https://www.bible.com/bible/111/PSA.63.NIV"),
            ScriptureData("May these words of my mouth and this meditation of my heart be pleasing in your sight, Lord, my Rock and my Redeemer.", "Psalm 19:14", "https://www.bible.com/bible/111/PSA.19.NIV"),
            ScriptureData("The Lord bless you and keep you; the Lord make his face shine on you and be gracious to you.", "Numbers 6:24-25", "https://www.bible.com/bible/111/NUM.6.NIV"),
            ScriptureData("The Lord turn his face towards you and give you peace.", "Numbers 6:26", "https://www.bible.com/bible/111/NUM.6.NIV"),
            ScriptureData("To him who is able to keep you from stumbling and to present you before his glorious presence without fault and with great joy...", "Jude 1:24", "https://www.bible.com/bible/111/JUD.1.NIV"),
            ScriptureData("May the grace of the Lord Jesus Christ, and the love of God, and the fellowship of the Holy Spirit be with you all.", "2 Corinthians 13:14", "https://www.bible.com/bible/111/2CO.13.NIV"),
            ScriptureData("The Lord is with me; I will not be afraid. What can mere mortals do to me?", "Psalm 118:6", "https://www.bible.com/bible/111/PSA.118.NIV"),
            ScriptureData("Great peace have those who love your law, and nothing can make them stumble.", "Psalm 119:165", "https://www.bible.com/bible/111/PSA.119.NIV"),
            ScriptureData("And we know that in all things God works for the good of those who love him...", "Romans 8:28", "https://www.bible.com/bible/111/ROM.8.NIV"),
            ScriptureData("He will wipe every tear from their eyes. There will be no more death or mourning or crying or pain, for the old order of things has passed away.", "Revelation 21:4", "https://www.bible.com/bible/111/REV.21.NIV")
        )
    )

    // LOGIC: Handles fetching specific topics or flattening the list for 'Random'
    fun getVerse(topicKey: String): ScriptureData {
        val pool = if (topicKey == "Surprise Me! (Random)" || topicKey == "Random" || !categories.containsKey(topicKey)) {
            categories.values.flatten()
        } else {
            categories[topicKey] ?: emptyList()
        }
        
        // Fallback if list is empty
        return if (pool.isNotEmpty()) pool.random() else ScriptureData("Be still, and know that I am God.", "Psalm 46:10", "https://www.bible.com/bible/111/PSA.46.NIV")
    }
}
