import Image from "next/image";

export default function Home() {

	const testAPK="/downloads/app-release.apk"
	const liveAPK="https://play.google.com/store/apps/details?id=jp.lab75.galxywatch"

  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24">
		<div>
			<a href={testAPK} className="font-medium text-blue-600 dark:text-blue-500 hover:underline">
				Download APK
			</a>
		</div>
		<div>
			<span>powered by</span>
			<Image src="/img/splash.png" width={100} height={100} alt="ESA x SAMSUNG"/>
		</div>
    </main>
  );
}
