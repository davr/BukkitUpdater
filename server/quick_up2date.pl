#! /usr/bin/perl

use strict;
use LWP::UserAgent;
use Perl::Version;
use DBI;

my ($over_all, $new_plugins, $updated_plugins, $ref_version, $link) = (0, 0, 0, 0, "");
require HTTP::Request;

my $dsn = "dbi:mysql:Bukkit:localhost:3306";
my $con = DBI->connect($dsn, "Bukkit", "zztquBLEjpRW7yYJ") or die("Unable to connect: $DBI::errstr\n");

my $ua = LWP::UserAgent->new;
my $request = HTTP::Request->new(GET => 'http://forums.bukkit.org/forums/plugin-releases.17/index.rss');
my $response = $ua->request($request);
my $str = $response->content();

if ($response->is_success()) {
#<item>
#      <title>[ADMIN] PreKick v0.7.1 - Whitelist with custom messages [953]</title>
#      <pubDate>Tue, 19 Jul 2011 09:10:55 +0000</pubDate>
#      <link>http://forums.bukkit.org/threads/admin-prekick-v0-7-1-whitelist-with-custom-messages-953.15507/</link>
#      <guid>http://forums.bukkit.org/threads/admin-prekick-v0-7-1-whitelist-with-custom-messages-953.15507/</guid>
#      <author>WildN00b</author>
#      <dc:creator>WildN00b</dc:creator>
#      <slash:comments>171</slash:comments>
#</item>
	my @title = $str =~ /<title>(.*?)<\/title>/g;
	my @link = $str =~ /<link>(.*?)<\/link>/g;
	for (my $i=0;$i<@title;$i++) {
		if ($title[$i] =~ /^\[.+?\][\s|](.+?)\s.*?[v|V|]([0-9\.\-\_]*?)\s(.*?)\[.*?\]$/) {
			my ($plugin, $version) = ($1, $2);
			$version =~ s/[^\d\.]//g;
			if ($version =~ /^\./) {
				$version = substr $version, 1, length($version);
			}
			my $query = "SELECT version FROM plugins WHERE plugin LIKE '$plugin'";
			my $handle = $con->prepare($query);
			$handle->execute() or die "Error: $!";
			if ($handle->rows() eq 0) {
				$query = "INSERT INTO plugins (plugin, version) VALUES (\"$plugin\", \"$version\")";
				$handle = $con->prepare($query);
				$handle->execute() or die "Error: $!";
				$new_plugins++;
				$ref_version = 0;
			} else {
				$ref_version = $handle->fetchrow_array;
				if (version_comparision($version, $ref_version)) {
					$query = "UPDATE plugins SET version = \"$version\" WHERE plugin = \"$plugin\"";
					$handle = $con->prepare($query);
					$handle->execute() or die "Error: $!";
					#debug
					print "[update inc]\n";
					$updated_plugins++;
				}
			}
			# now add a link if there is one
			$request = HTTP::Request->new(GET => $link[$i]);
			$response = $ua->request($request);
			$str = $response->content();
			if ($response->is_success()) {
				if ($str =~ /(htt[ps|p]:\/\/[\w\d\/\.\-\_\s]*?\/$plugin\.jar)/ig) {
					$link = $1;
					$query = "SELECT plugin
					FROM Bukkit_Token
					WHERE plugin LIKE '$plugin'";
					$handle = $con->prepare($query);
					$handle->execute();

					if ($handle->rows() == 0) {
						$query = "INSERT INTO Bukkit_Token (token, plugin, link)
						VALUES('*', '$plugin', '$link')";
					} else {
						$query = "UPDATE Bukkit_Token
						SET token = '*', link = '$link'
						WHERE plugin LIKE '$plugin'";
					}

					$handle = $con->prepare($query);
                                        $handle->execute();
				} else {
 					$link = "";
					$query = "SELECT plugin
					FROM Bukkit_Token
					WHERE plugin LIKE '$plugin'";
					$handle = $con->prepare($query);
					$handle->execute();

					if (!$handle->rows() == 0) {
						#debug
						print "[link delete inc]\n";
						$query = "DELETE FROM Bukkit_Token WHERE token = '*' AND plugin = '$plugin' LIMIT 1";
						$handle = $con->prepare($query);
						$handle->execute();
					}
				}
			}

			print "[Plugin]: $plugin\n\t[Forum]: $version\n\t[Database]: $ref_version\n\t[Link]: $link\n\n";
			$over_all++;
		}
	}
	print "\n[New plugins]: $new_plugins\n[Updated plugins]: $updated_plugins\n[From]: $over_all\n";
} else {
	print "false";
}

sub version_comparision {
	my ($v1, $v2) = (0, 0);
	eval {
		$v1 = Perl::Version->new( shift );
		$v2 = Perl::Version->new( shift );
	};
	# warn if something happens
	warn $@ if $@;

	#debug
	#print "[version comparision $v1 :: $v2]\n";
	if ($v1->vcmp( $v2 ) > 0) {
		return 1;
	}

	return 0;
}
