class Dzienniczek < Formula
  desc "Agent-friendly CLI for VULCAN, eduVULCAN and Librus"
  homepage "https://github.com/szponciciel04/DzienniczekSzpontniczek"
  url "https://github.com/szponciciel04/DzienniczekSzpontniczek.git", branch: "main"
  version "0.2.0"
  license "MIT"

  depends_on "openjdk@17"

  def install
    ENV["JAVA_HOME"] = Formula["openjdk@17"].opt_prefix
    system "./gradlew", ":cli:installDist", "--no-daemon"
    libexec.install Dir["cli/build/install/dzienniczek/*"]
    bin.write_env_script libexec/"bin/dzienniczek", JAVA_HOME: Formula["openjdk@17"].opt_prefix
    bash_completion.install "completions/dzienniczek.bash" => "dzienniczek"
    zsh_completion.install "completions/_dzienniczek"
    fish_completion.install "completions/dzienniczek.fish"
  end

  test do
    assert_match "0.2.0", shell_output("#{bin}/dzienniczek version --format table")
  end
end
