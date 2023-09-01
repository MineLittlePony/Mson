 # M-SON
 
 [![Current Version](https://img.shields.io/github/v/release/MineLittlePony/Mson)](https://github.com/MineLittlePony/Mson/releases/latest)
[![Build Status](https://img.shields.io/github/actions/workflow/status/MineLittlePony/Mson/gradle-build.yml)](https://github.com/MineLittlePony/Mson/actions/workflows/gradle-build.yml)
![Downloads](https://img.shields.io/github/downloads/MineLittlePony/Mson/total.svg?color=yellowgreen)
[![Discord Server](https://img.shields.io/discord/182490536119107584.svg?color=blueviolet)](https://discord.gg/HbJSFyu)
![License](https://img.shields.io/github/license/MineLittlePony/Mson)
![](https://img.shields.io/badge/api-fabric-orange.svg)

 ## /ˈmeɪs(ə)n/

> _noun_  
> noun: **mason**; plural noun: **masons**; noun: **Mason**; plural noun: **Masons**
> 1. a person skilled in cutting, dressing, laying stone in buildings.  
>    _"the chief mason at Westminster Abby"_
> 2. a Freemason  
>    _"a Mason's handshake"_
> 3. a pormantau of Model and JSON  
    _"check out these awesome msons, yo"_
> 3. a modelling Minecraft library that combines entity models and behaviours with the ability to load json files  
>   _"dude, you gotta use M-Son"_

> verb  
>  -   build from or stengthen with stone  
>     _"the other building was masoned up out of hewn limestone"_
>  - cut or dress (stone).  
>     _"one course of massive stones, roughly masoned"_

### What is Mson?

[Wiki](https://github.com/MineLittlePony/Mson/wiki) | [Model Language Definition](https://github.com/MineLittlePony/Mson/tree/1.20/doc)

Mson is a fabric library/mod for defining and loading entity models through configurable json files.

It makes modders' lives easier by taking the model geometry out of the code and putting it in a place where it's separated from game logic,
and it makes player's lives better by making it possible for your models to be customised and replaced using nothing more than a resourcepack.


## Getting Started [ResourcePack & Mod Creators]

When MSON is installed, every entity model in the game can be loaded from an equivalent json file in the `assets/<namespace>/models/entity/mson` folder. Check [this folder](https://github.com/MineLittlePony/Mson/tree/1.20/src/test/resources/assets/minecraft/models/entity/mson) for a short list of example models made to closely (and in some cases not very closely) match the equivalent model for a limited few vanilla entities.

For a more complete list, and instructions on how to convert models already loaded into the game, check the [Sample 1.20.1 Models](https://github.com/MineLittlePony/Mson/wiki/Sample-1.20.1-Models) wiki page.

## Getting Started [Mod Creators]

Want to register your own models to load with mson? Doing so is as simple as:

1) `assets/<modid>/models/entity/my_model.json`

2) `static ModelKey<MyEntityModel<MyEntity> MY_ENTITY_MODEL = Mson.getInstance().registerModel(new Identifier("mymod", "my_model"), MyEntityModel::new);`

3) `MyEntityModel<MyEntity> model = MyModels.MY_ENTITY_MODEL.createModel();`

Want to create a humanoid model? Mson already bundles models for steve, alex, and a simplistic quadruped, so just specify `{ "parent": "mson:steve" }` as your starting model and add override for each body part as you go. Check [here](https://github.com/MineLittlePony/Mson/tree/1.20/src/test/resources/assets) for examples!

