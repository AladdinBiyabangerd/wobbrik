import AppKit
let dir=CommandLine.arguments[1]
let sheets=try FileManager.default.contentsOfDirectory(atPath:dir).filter {$0.hasPrefix("catalog-") && $0.hasSuffix(".png")}.count
for start in stride(from:0,to:sheets,by:4) {
 let count=min(4,sheets-start)
 let rep=NSBitmapImageRep(bitmapDataPlanes:nil,pixelsWide:585*count,pixelsHigh:1266,bitsPerSample:8,samplesPerPixel:4,hasAlpha:true,isPlanar:false,colorSpaceName:.deviceRGB,bytesPerRow:0,bitsPerPixel:0)!
 NSGraphicsContext.saveGraphicsState(); NSGraphicsContext.current=NSGraphicsContext(bitmapImageRep:rep)
 for i in 0..<count {NSImage(contentsOfFile:dir+String(format:"/catalog-%02d.png",start+i))!.draw(in:NSRect(x:i*585,y:0,width:585,height:1266))}
 NSGraphicsContext.restoreGraphicsState()
 try rep.representation(using:.png,properties:[:])!.write(to:URL(fileURLWithPath:dir+"/board-\(start).png"))
}
