import discord
from discord import app_commands
from discord.ext import commands
import json
import os

LEGITCHECK_LICZNIK = "legitcount.json"
KANAL_LEGITCHECKS = 123456789012345678
ROLA_LEGITCHECK = 1425073322529652787
ROLA_RESET = 1424049392557625507

def load_count():
    if not os.path.exists(LEGITCHECK_LICZNIK):
        with open(LEGITCHECK_LICZNIK, "w") as f:
            json.dump({"count": 0}, f)
        return 0
    with open(LEGITCHECK_LICZNIK, "r") as f:
        data = json.load(f)
    return data.get("count", 0)

def save_count(value):
    with open(LEGITCHECK_LICZNIK, "w") as f:
        json.dump({"count": value}, f, indent=4)

class LegitCheck(commands.Cog):
    def __init__(self, bot):
        self.bot = bot

    @app_commands.command(name="legitcheck", description="Wyslij dowod zamowienia")
    @app_commands.describe(link="Link dowodu z linkiem do imgura")
    async def legitcheck(self, interaction: discord.Interaction, link: str):
        if ROLA_LEGITCHECK not in [r.id for r in interaction.user.roles]:
            return await interaction.response.send_message("<a:nexa_alert:1425079901387755591> Brak uprawnień (nexacode.legitchecks).", ephemeral=True)

        await interaction.response.defer(thinking=True)
        count = load_count() + 1
        save_count(count)

        opis = (
            "```・Nexacode - Potwierdzenie zakupu.```\n\n"

            f"<:gif:1425079898447548437> Czlonek nexateamu: **{interaction.user.name}**\n\n"

            f"<:puchar:1425079902633459754> Jest to nasz: ``{count}`` dowód.\n\n"

            "**<a:luckyblok:1425079899957362840> Klient otrzymał swoje zamówienie, dowód poniżej.**\n\n"
        )

        embed = discord.Embed(description=opis, color=discord.Color.dark_gray())
        embed.set_author(name=" Nexacode", icon_url="https://i.imgur.com/zmien.png")
        embed.set_image(url=link)
        embed.set_footer(text="**Dziekujemy za otrzymanie Twojego zaufania :)**")
        await interaction.followup.send(embed=embed)

        channel = interaction.guild.get_channel(KANAL_LEGITCHECKS)
        if channel:
            try:
                await channel.edit(name=f"✅〢legitchecks➜{count}")
            except:
                pass

    @app_commands.command(name="legit_resetuj", description="Resetuje licznik legitchecków")
    async def legit_resetuj(self, interaction: discord.Interaction):
        if ROLA_RESET not in [r.id for r in interaction.user.roles]:
            return await interaction.response.send_message("<a:nexa_alert:1425079901387755591> Brak uprawnień (all.perm).", ephemeral=True)

        save_count(0)
        channel = interaction.guild.get_channel(KANAL_LEGITCHECKS)
        if channel:
            try:
                await channel.edit(name="✅〢legitchecks➜0")
            except:
                pass
        await interaction.response.send_message(" Licznik legitchecków został zresetowany.")

async def setup(bot):
    await bot.add_cog(LegitCheck(bot))
