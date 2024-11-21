package net.muridemo.soteriology.data

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.data.event.GatherDataEvent
import java.io.{ByteArrayOutputStream, IOException, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

import com.google.gson.JsonElement
import com.google.gson.stream.JsonWriter
import com.mojang.serialization.JsonOps
import net.minecraft.Util
import net.minecraft.data.CachedOutput
import net.minecraft.util.GsonHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.google.common.hash.HashingOutputStream
import com.google.common.hash.Hashing
import scala.util.Using
import net.muridemo.soteriology.Soteriology

@Mod.EventBusSubscriber(modid = Soteriology.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
object DataGenerators {
  @SubscribeEvent
  def gatherData(event: GatherDataEvent) = {
    val gen = event.getGenerator()
    val existingFileHelper = event.getExistingFileHelper()

    gen.addProvider(event.includeServer(), TraitsProvider(gen))
  }

  def saveStable(cache: CachedOutput, json: JsonElement, path: Path) = {
    CompletableFuture.runAsync {() =>
      try {
        val baos = ByteArrayOutputStream()
        val haos = HashingOutputStream(Hashing.murmur3_32_fixed(), baos)

        Using(JsonWriter(OutputStreamWriter(haos, StandardCharsets.UTF_8))) { jsonwriter =>
          jsonwriter.setSerializeNulls(false)
          jsonwriter.setIndent("  ")
          GsonHelper.writeValue(jsonwriter, json, null)
        }

        cache.writeIfNeeded(path, baos.toByteArray, haos.hash())
      } catch {
        case ioexception: IOException =>
          Soteriology.LOGGER.error("Failed to save file to {}", path, ioexception)
      }
    }
  }
}
