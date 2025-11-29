# Arcanomech Agent Notes

## Mana и тик-логика
- Сеть маны строится через `ManaNetworkManager` и кэш `ManaGraphCache`; компоненты пересобираются при пометке dirty блоками (установка/слом, изменение side config, загрузка/выгрузка чанка) и логируют размер/ману/ёмкость/долю.
- Балансировка выполняется тиком узлов (`ManaBatteryBlockEntity.tick`, `ManaCableBlockEntity.tick`) и гоняет ману малыми порциями с лимитами IO узлов и `Balance.CABLE_IO` на каждом ребре пути, выравнивая заполнение до общей доли без осцилляций и без двойного перетока за тик.
- Кабели — узлы с нулевой емкостью, служат только для топологии; ману хранят батареи и другие емкостные узлы. Команда `/am mana info` печатает id компоненты, размеры, запас, fill%, рассчитанную целевую долю и IO лимит выбранного узла.

## Build & Run
- Сборка: `./gradlew clean :arcanomech:build` (алиас: `./gradlew clean buildAll`)
- Клиент: `./gradlew :arcanomech:runClient`
- Тесты: `./gradlew :arcanomech:test`
- Toolchain: JDK 21 с `options.release = 17` для компиляции.
