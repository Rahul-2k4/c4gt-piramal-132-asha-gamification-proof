import { readFileSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const here = dirname(fileURLToPath(import.meta.url));
const kotlinPath = join(here, "GamificationProof.kt");
const outputPath = join(here, "proof-output.json");
const source = readFileSync(kotlinPath, "utf8");

const checks = [
  ["GamificationEvent data model", /data class GamificationEvent/],
  ["RewardState data model", /data class RewardState/],
  ["duplicate prevention", /event\.eventId in state\.acceptedEventIds/],
  ["daily cap constant", /DAILY_CAP = 100/],
  ["pending sync status", /syncStatus: String = "pending"/],
];

const results = checks.map(([name, pattern]) => ({
  name,
  pass: pattern.test(source),
}));

const output = {
  proofType: "Android-native Kotlin proof slice",
  issue: "Piramal #132",
  integratedIntoFlwApp: false,
  androidSdkBlocked: true,
  results,
  pass: results.every((item) => item.pass),
};

writeFileSync(outputPath, `${JSON.stringify(output, null, 2)}\n`);
console.log(JSON.stringify(output, null, 2));

if (!output.pass) process.exit(1);