{pkgs ? import <nixpkgs> {} }:
with pkgs;
mkShell {
  buildInputs = [ jdk gradle ];
}
