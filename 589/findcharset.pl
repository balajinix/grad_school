#!/usr/bin/perl

#******************************************************************************
#*      Course:		  MATH 589					      *
#* 	Program:	  findcharset.pl 				      *
#*	Author:		  Balaji Ganesan				      *
#*	Description:  Program to crawl the web and fetch the character sets   *
#******************************************************************************

use LWP::Simple;
use LWP::UserAgent;
use HTTP::Request;
use HTTP::Response;
use HTML::LinkExtor;
use UNIVERSAL qw(isa);

# get command line arguments
if ($#ARGV ne 2)
{
	die "Usage: findcharset url displayerror depth\n";
}

$URL = $ARGV[0];
$DEBUG = $ARGV[1];
$depth = $ARGV[2];

# using the useragent class get the url
$browser = LWP::UserAgent->new();
$browser->timeout(10);
my $request = HTTP::Request->new(GET => $URL);
my $response = $browser->request($request);
if ($response->is_error()) {die "$response->status_line";}
$contents = $response->content();

%URLcharset = ();
%URLhash = ();
$visited = 0;

# populate the required hash and yrl queue
$URLhash{$URL} = 0;
unshift (@URLqueue, $URL); 

$thisURL = pop(@URLqueue);

$x=1;
$level = 0;

if (($DEBUG eq 1) && ($depth > 0))
{
	print "\n--------------------------------------------------------\n";
	print "   Crawler is adding the following urls to the queue\n";
	print "--------------------------------------------------------\n\n";
}

# While there's still a URL in our queue unvisited
while(($thisURL ne "")&& $depth > -1)
{

	$count = 0;
	while(($key,$value) = each(%URLhash))
	{
		$count ++;
	}


	if($visited==$x)
	{
		$x=$x+($count-$visited);
		$depth--;
		$level++;
	}

	my $request = HTTP::Request->new(GET =>$thisURL);
	my $response = $browser->request($request);
	if ($response->is_error()) 
	{
		if ($DEBUG eq 1) {printf "%s\n", $response->status_line;}
	}
	else
	{
		# get the character set
		$contents = $response->content();
		$charset = getCharset($response);
		$thisURL =~ m|(\w+)://([^/:]+)(:\d+)?/(.*)|;

		if ($charset eq "") {$charset = "unknown"};
		
		if ($2 eq "")
		{
			if (($URLcharset{$thisURL} eq "") || 
				($URLcharset{$thisURL} eq "unknown"))
			{
				$URLcharset{$thisURL} = $charset;				
			}
		}
		else
		{
			if (($URLcharset{$2} eq "") || 
				($URLcharset{$2} eq "unknown"))
			{
				$URLcharset{$2} = $charset;				
			}
		}
		
		if ($depth > 0)
		{
			my ($page_parser) = HTML::LinkExtor->new(undef,$thisURL);
			$page_parser->parse($contents)->eof;
			@orglinks = $page_parser->links;

			foreach $link (@orglinks) 
			{
				if (($$link[2] =~ /^mailto:/ ) || ($$link[2] =~ /^ftp:/ ))
				{
					#ignore mailto and ftp links
				}
				else
				{
					#print "$$link[2]\n";	
					push(@links,$link);
				}
			}

			# populate the URL queue with new links
			foreach $link (@links)
			{
				$newURL=$$link[2];
				
				if($URLhash{$newURL} > 0)
				{
					# Increment the count for URLs we've already checked out
					$URLhash{$newURL}++;
				}
				else
				{
					# Add a zero record for URLs we haven't encountered
					$URLhash{$newURL}=0;
					unshift (@URLqueue, $newURL); 
					if ($DEBUG eq 1) {print "$newURL\n"};
				}

			}
		}
	}
	
	# increment the hash value for this visit
	$URLhash{$thisURL} ++;
	$visited ++;
	$thisURL = pop(@URLqueue);
}

# print the result
&print_result(%URLcharset);

# the following functions are used to get the character set
sub getCharset {
    my ($response) = @_;
    return getCharsetFromHeader($response) || getCharsetFromMeta($response);
}

sub getCharsetFromHeader {
    my ($response) = @_;

    my $headers = $response->headers();
   
    if(isa $headers->{'content-type'}, "ARRAY") {
	$cth = $headers->{'content-type'}->[1];
    } else {
	$cth = $headers->{'content-type'};
    }
    my ($charset) = $cth =~ /charset=([^\s";]*)/ ;
    return lc($charset);
}

sub getCharsetFromMeta {
    my ($response) = @_;
    getCharsetFromMetaString($response->content);
}

sub getCharsetFromMetaString {
    my ($string) = @_;
    while($string =~ /(<meta.*?>)/gis) {
	my $meta = $1;
	if(my ($charset) = $meta =~ /charset=([^\s";]*)/i) {
	    return lc($charset);
	}
    }
    return;
}

# this function prints the results
sub print_result
{
	%charsetsummary = ();

	print "\n--------------------------------------------------------\n";
	print "\tURL\t\t\tCharacter Set\n";
	print "--------------------------------------------------------\n";
	
	local(%URLcharset) = @_;
	local($key, $value);
	while(($key, $value) = each(%URLcharset))
	{
		print "\t",$key,"\t",$value,"\n";
		$charsetsummary{$value}++;
	}

	if ($ARGV[2] ne 0)
	{
		# print the summary
		print "\n--------------------------------------------------------\n";
		print "\tCount\tCharacter Set\n";
		print "--------------------------------------------------------\n";
		while(($key, $value) = each(%charsetsummary))
		{
			print "\t",$value,"\t",$key,"\n";
		}
		print "--------------------------------------------------------\n\n";

	}
	else
	{
		print "--------------------------------------------------------\n\n";
	}
	
	return "";
}
